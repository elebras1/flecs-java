import org.gradle.internal.os.OperatingSystem
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
    id("java")
}

group = "org"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

val flecsVersion = "4.1.2"
val flecsDir = layout.buildDirectory.dir("flecs").get().asFile
val flecsSourceDir = File(flecsDir, "flecs-$flecsVersion")

val flecsHeaderFile = File(flecsSourceDir, "distr/flecs.h")
val flecsCFile = File(flecsSourceDir, "distr/flecs.c")

val generatedSourcesDir = file("src/main/java")

val os = OperatingSystem.current()
val userHome = System.getProperty("user.home")

val jextractExecutable = when {
    os.isWindows -> "$userHome/.local/jextract/jextract-25/bin/jextract.exe"
    else -> "$userHome/.local/jextract/jextract-25/bin/jextract"
}

val nativeLibName = when {
    os.isWindows -> "flecs.dll"
    os.isMacOsX -> "libflecs.dylib"
    else -> "libflecs.so"
}

val downloadFlecs by tasks.registering {
    description = "Download Flecs release from GitHub"
    group = "flecs"

    val outputFile = File(flecsDir, "flecs-${flecsVersion}.tar.gz")
    outputs.file(outputFile)

    doLast {
        flecsDir.mkdirs()
        val url = "https://github.com/SanderMertens/flecs/archive/refs/tags/v${flecsVersion}.tar.gz"
        println("Downloading Flecs $flecsVersion from $url")

        URL(url).openStream().use { input ->
            Files.copy(input, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        println("Flecs downloaded to: ${outputFile.absolutePath}")
    }
}

val extractFlecs by tasks.registering(Copy::class) {
    description = "Extract Flecs archive"
    group = "flecs"
    dependsOn(downloadFlecs)

    from(tarTree(downloadFlecs.get().outputs.files.singleFile))
    into(flecsDir)

    doLast {
        println("Flecs extracted to: ${flecsSourceDir.absolutePath}")
        if (!flecsCFile.exists()) {
            println("WARNING: ${flecsCFile.absolutePath} does not exist!")
            println("Extracted directory content:")
            flecsSourceDir.walk().maxDepth(2).forEach { println("  ${it.relativeTo(flecsSourceDir)}") }
        }
    }
}

val compileFlecsNative by tasks.registering(Exec::class) {
    description = "Compile the Flecs C native library"
    group = "flecs"

    dependsOn(extractFlecs)

    doFirst {
        if (!flecsCFile.exists()) {
            throw GradleException(
                "Flecs source file not found: ${flecsCFile.absolutePath}"
            )
        }
    }

    workingDir(flecsSourceDir)
    inputs.file(flecsCFile)

    val outputNativeFile = File(flecsSourceDir, "distr/$nativeLibName")
    outputs.file(outputNativeFile)

    val compileCommand = when {
        os.isWindows -> listOf(
            "gcc",
            "-shared",
            "-o", outputNativeFile.absolutePath,
            flecsCFile.absolutePath,
            "-Ofast",
            "-std=c99",
            "-DFLECS_SHARED"
        )
        os.isMacOsX -> listOf(
            "clang",
            "-shared",
            "-fPIC",
            "-o", outputNativeFile.absolutePath,
            flecsCFile.absolutePath,
            "-Ofast",
            "-std=c99",
            "-DFLECS_SHARED"
        )
        else -> listOf(
            "gcc",
            "-shared",
            "-fPIC",
            "-o", outputNativeFile.absolutePath,
            flecsCFile.absolutePath,
            "-Ofast",
            "-std=c99",
            "-DFLECS_SHARED",
            "-D_POSIX_C_SOURCE=200809L",
            "-D_DEFAULT_SOURCE",
            "-lm",
            "-lrt",
            "-lpthread"
        )
    }

    commandLine(compileCommand)

    doLast {
        println("Native Flecs library compiled: ${outputNativeFile.absolutePath}")
    }
}

val generateFlecsBindings by tasks.registering(Exec::class) {
    description = "Generate Java FFM bindings using jextract"
    group = "flecs"

    dependsOn(extractFlecs)

    inputs.file(flecsHeaderFile)
    outputs.dir(generatedSourcesDir)

    doFirst {
        if (!flecsHeaderFile.exists()) {
            throw GradleException("Flecs header file not found: ${flecsHeaderFile.absolutePath}")
        }
        generatedSourcesDir.mkdirs()
        println("Using external jextract executable")
    }

    commandLine(
        jextractExecutable,
        "--output", generatedSourcesDir.absolutePath,
        "-t", "com.github.elebras1.flecs.generated",
        "-I", File(flecsSourceDir, "distr").absolutePath,
        flecsHeaderFile.absolutePath
    )

    doLast {
        println("Java FFM bindings generated in: ${generatedSourcesDir.absolutePath}")
    }
}

val copyFlecsNative by tasks.registering(Copy::class) {
    description = "Copy compiled Flecs native library to resources"
    group = "flecs"

    dependsOn(compileFlecsNative)

    from(File(flecsSourceDir, "distr")) {
        include(nativeLibName)
    }

    into(layout.buildDirectory.dir("resources/main").get().asFile)

    doLast {
        println("Native library copied to resources")
    }
}

sourceSets {
    main {
        java {
            srcDir(generatedSourcesDir)
        }
        resources {
            srcDir(layout.buildDirectory.dir("resources/main"))
        }
    }
}

tasks.compileJava {
    dependsOn(copyFlecsNative)

    options.release.set(25)
    options.compilerArgs.addAll(listOf(
        "--enable-preview"
    ))
}

tasks.processResources {
    dependsOn(copyFlecsNative)
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--enable-preview", "--enable-native-access=ALL-UNNAMED")
}

val cleanFlecs by tasks.registering(Delete::class) {
    description = "Delete Flecs downloaded, compiled and generated files"
    group = "flecs"

    delete(flecsDir)
    delete(generatedSourcesDir)
    delete(layout.buildDirectory.dir("resources/main"))
}

tasks.clean {
    dependsOn(cleanFlecs)
}

val regenerateFlecs by tasks.registering {
    description = "Clean and fully regenerate Flecs bindings"
    group = "flecs"

    dependsOn(cleanFlecs, compileFlecsNative, generateFlecsBindings)

    tasks.findByName("compileFlecsNative")?.mustRunAfter(cleanFlecs)
    tasks.findByName("generateFlecsBindings")?.mustRunAfter(cleanFlecs)
}