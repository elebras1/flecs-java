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
    implementation("com.palantir.javapoet:javapoet:0.9.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

val flecsVersion = "4.1.2"
val flecsDir = layout.buildDirectory.dir("flecs").get().asFile
val flecsSourceDir = File(flecsDir, "flecs-$flecsVersion")

val flecsHeaderFile = File(flecsSourceDir, "distr/flecs.h")
val flecsCFile = File(flecsSourceDir, "distr/flecs.c")

val generatedSourcesDir = file("src/main/generated")
val annotationGeneratedDir = layout.buildDirectory.dir("generated/sources/annotationProcessor/java/main").get().asFile

val os = OperatingSystem.current()
val userHome = System.getProperty("user.home")

val jextractExecutable = when {
    os.isWindows -> "$userHome/.local/jextract/jextract-25/bin/jextract.exe"
    else -> "$userHome/.local/jextract/jextract-25/bin/jextract"
}

val nativeLibName = "libflecs.so"

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
            "-DFLECS_SHARED",
            "-DNDEBUG"
        )
        os.isMacOsX -> listOf(
            "clang",
            "-shared",
            "-fPIC",
            "-o", outputNativeFile.absolutePath,
            flecsCFile.absolutePath,
            "-Ofast",
            "-std=c99",
            "-DFLECS_SHARED",
            "-DNDEBUG"
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
            "-DNDEBUG",
            "-D_POSIX_C_SOURCE=200809L",
            "-D_DEFAULT_SOURCE",
            "-lm",
            "-lrt",
            "-lpthread"
        )
    }

    commandLine(compileCommand)
}

val generateFlecsBindings by tasks.registering(Exec::class) {
    description = "Generate Java FFM bindings using jextract"
    group = "flecs"

    dependsOn(compileFlecsNative)

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
        "-t", "com.github.elebras1.flecs",
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

    into(layout.buildDirectory.dir("natives").get().asFile)

    doLast {
        println("Native library copied to natives")
    }
}

sourceSets {
    main {
        java {
            srcDir(generatedSourcesDir)
            srcDir(annotationGeneratedDir)
        }
        resources {
            srcDir(layout.buildDirectory.dir("natives"))
        }
    }
}

val compileProcessor by tasks.registering(JavaCompile::class) {
    source = fileTree("src/main/java") {
        include("**/processor/**")
        include("**/annotation/**")
    }
    classpath = configurations.compileClasspath.get()
    destinationDirectory.set(layout.buildDirectory.dir("classes/java/processor"))
    options.release.set(25)
    options.compilerArgs.add("--enable-preview")
}

val copyProcessorResources by tasks.registering(Copy::class) {
    from("src/main/resources/META-INF/services") {
        include("javax.annotation.processing.Processor")
    }
    into(layout.buildDirectory.dir("classes/java/processor/META-INF/services"))
    dependsOn(compileProcessor)
}


tasks.compileJava {
    dependsOn(copyFlecsNative, copyProcessorResources)

    exclude("**/processor/**")

    options.release.set(25)

    val processorClasspath = files(
        compileProcessor.get().destinationDirectory.get().asFile,
        configurations.compileClasspath.get()
    )

    options.annotationProcessorPath = processorClasspath

    options.compilerArgs.addAll(listOf(
        "--enable-preview",
        "-s", annotationGeneratedDir.absolutePath
    ))

    doFirst {
        annotationGeneratedDir.mkdirs()
    }

    doLast {
        copy {
            from(layout.buildDirectory.dir("classes/java/processor"))
            into(layout.buildDirectory.dir("classes/java/main"))
        }
    }
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
    delete(layout.buildDirectory.dir("natives"))
}

tasks.clean {
    dependsOn(cleanFlecs)
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
