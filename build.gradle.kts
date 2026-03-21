import org.gradle.internal.os.OperatingSystem
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
    id("java")
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

group = "io.github.elebras1"
version = System.getenv("GITHUB_REF_NAME")?.removePrefix("v") ?: project.findProperty("version") as String? ?: "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.palantir.javapoet:javapoet:0.12.0")
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
    withJavadocJar()
}

val flecsVersion = "4.1.5"
val flecsDir = layout.buildDirectory.dir("flecs").get().asFile
val flecsSourceDir = File(flecsDir, "flecs-$flecsVersion")

val flecsHeaderFile = File(flecsSourceDir, "distr/flecs.h")
val flecsCFile = File(flecsSourceDir, "distr/flecs.c")

val generatedSourcesDir = file("src/main/generated")
val annotationGeneratedMainDir = layout.buildDirectory.dir("generated/sources/annotationProcessor/java/main").get().asFile
val annotationGeneratedTestDir = layout.buildDirectory.dir("generated/sources/annotationProcessor/java/test").get().asFile

val os : OperatingSystem = OperatingSystem.current()
val userHome : String = System.getProperty("user.home")

val jextractExecutable = when {
    os.isWindows -> "$userHome/.local/jextract-25/bin/jextract.exe"
    else -> "$userHome/.local/jextract-25/bin/jextract"
}

val nativeLibName = when {
    os.isWindows -> "flecs.dll"
    os.isMacOsX -> "libflecs.dylib"
    else -> "libflecs.so"
}

val nativeArch = project.findProperty("NATIVE_ARCH") as String? ?: when {
    os.isLinux && System.getProperty("os.arch") in listOf("amd64", "x86_64") -> "linux-x64"
    os.isLinux && System.getProperty("os.arch") in listOf("aarch64", "arm64") -> "linux-aarch64"
    os.isWindows && System.getProperty("os.arch") in listOf("amd64", "x86_64") -> "windows-x64"
    os.isWindows && System.getProperty("os.arch") == "aarch64" -> "windows-aarch64"
    os.isMacOsX && System.getProperty("os.arch") == "aarch64" -> "macos-aarch64"
    os.isMacOsX && System.getProperty("os.arch") in listOf("amd64", "x86_64") -> "macos-x64"
    else -> "unknown"
}

val archFlag = when (nativeArch) {
    "linux-x64", "windows-x64" -> "-march=x86-64-v2"
    "macos-x64" -> "-mtune=generic"
    "linux-aarch64", "windows-aarch64", "macos-aarch64" -> "-march=armv8-a"
    else -> "-march=native"
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

        URI(url).toURL().openConnection().getInputStream().use { input ->
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

    workingDir(flecsSourceDir)
    val outputNativeFile = layout.buildDirectory.dir("resources/main/natives/$nativeArch/$nativeLibName").get().asFile
    outputs.file(outputNativeFile)

    val compileCommand = when {
        os.isWindows -> listOf(
            "gcc",
            "-shared",
            "-o", outputNativeFile.absolutePath,
            flecsCFile.absolutePath,
            "-Ofast",
            archFlag,
            "-flto",
            "-fomit-frame-pointer",
            "-funroll-loops",
            "-std=c99",
            "-Dflecs_EXPORTS",
            "-DNDEBUG",
            "-lws2_32",
            "-ldbghelp"
        )
        os.isMacOsX -> listOf(
            "gcc",
            "-dynamiclib",
            "-o", outputNativeFile.absolutePath,
            flecsCFile.absolutePath,
            "-Ofast",
            archFlag,
            "-flto",
            "-fomit-frame-pointer",
            "-funroll-loops",
            "-std=c99",
            "-Dflecs_EXPORTS",
            "-DNDEBUG",
            "-framework", "CoreFoundation"
        )
        else -> listOf(
            "gcc",
            "-shared",
            "-fPIC",
            "-o", outputNativeFile.absolutePath,
            flecsCFile.absolutePath,
            "-Ofast",
            archFlag,
            "-flto",
            "-fomit-frame-pointer",
            "-funroll-loops",
            "-fno-semantic-interposition",
            "-fno-plt",
            "-std=c99",
            "-Dflecs_EXPORTS",
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
    description = "Generate Java FFM bindings using jextract (maintainer-only task, run when updating Flecs version)"
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

val validateGeneratedBindings by tasks.registering {
    group = "verification"
    doLast {
        if (!generatedSourcesDir.exists()) {
            throw GradleException("Bindings not found at: $generatedSourcesDir")
        }
    }
}

val generatorSourcesDir = file("src/generator/java")

val generateEachBase by tasks.registering(JavaExec::class) {
    description = "Generate QueryBase, SystemBuilderBase, ObserverBuilderBase and all typed each callback interfaces"
    group = "flecs"
    classpath = sourceSets["generator"].runtimeClasspath
    mainClass.set("com.github.elebras1.flecs.EachBaseGenerator")
    args(generatedSourcesDir.absolutePath)
    outputs.dir(generatedSourcesDir)
    doFirst {
        generatedSourcesDir.mkdirs()
    }
}

sourceSets {
    create("generator") {
        java.srcDir(generatorSourcesDir)
    }
    main {
        java {
            srcDir(generatedSourcesDir)
            srcDir(annotationGeneratedMainDir)
        }
    }
    test {
        java {
            srcDir(annotationGeneratedTestDir)
        }
    }
}

configurations["generatorCompileClasspath"].extendsFrom(configurations["compileClasspath"])
configurations["generatorRuntimeClasspath"].extendsFrom(configurations["runtimeClasspath"])

tasks.named("processResources") {
    dependsOn(compileFlecsNative)
}

val compileProcessor by tasks.registering(JavaCompile::class) {
    source = fileTree("src/main/java") {
        include("**/processor/**")
        include("**/annotation/**")
    }
    classpath = configurations.compileClasspath.get()
    destinationDirectory.set(layout.buildDirectory.dir("classes/java/processor"))
    options.release.set(25)
}

val copyProcessorResources by tasks.registering(Copy::class) {
    from("src/main/resources/META-INF/services") {
        include("javax.annotation.processing.Processor")
    }
    into(layout.buildDirectory.dir("classes/java/processor/META-INF/services"))
}

tasks.compileJava {
    dependsOn(validateGeneratedBindings, copyProcessorResources, compileProcessor)

    exclude("**/processor/**")
    exclude("**/annotation/**")

    options.release.set(25)

    val processorOutput = compileProcessor.get().destinationDirectory.get().asFile
    classpath = files(processorOutput) + classpath

    options.annotationProcessorPath = files(processorOutput) + configurations.runtimeClasspath.get()

    options.compilerArgs.addAll(listOf(
        "-s", annotationGeneratedMainDir.absolutePath
    ))

    doFirst {
        annotationGeneratedMainDir.deleteRecursively()
        annotationGeneratedMainDir.mkdirs()
    }
}

tasks.jar {
    dependsOn(compileProcessor, copyProcessorResources)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(compileProcessor.get().destinationDirectory)

    from(copyProcessorResources.get().destinationDir)

    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })

    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    manifest {
        attributes(
            "Implementation-Title" to "Flecs Java",
            "Implementation-Version" to project.version,
        )
    }
}

val cleanFlecs by tasks.registering(Delete::class) {
    description = "Delete Flecs downloaded, compiled and generated files"
    group = "flecs"

    delete(flecsDir)
}

tasks.clean {
    dependsOn(cleanFlecs)
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileTestJava {
    dependsOn(compileProcessor)
    val processorOutput = compileProcessor.get().destinationDirectory.get().asFile
    classpath = files(processorOutput) + classpath

    options.annotationProcessorPath = files(processorOutput) + configurations.runtimeClasspath.get()

    options.compilerArgs.addAll(listOf(
        "-s", annotationGeneratedTestDir.absolutePath
    ))

    doFirst {
        annotationGeneratedTestDir.deleteRecursively()
        annotationGeneratedTestDir.mkdirs()
    }
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.named("sourcesJar") {
    dependsOn(tasks.compileJava)
}

tasks.withType<Javadoc>().configureEach {
    (options as? StandardJavadocDocletOptions)?.apply {
        addBooleanOption("Xdoclint:none", true)
        addStringOption("quiet", "-quiet")
    }
}

configure<io.github.gradlenexus.publishplugin.NexusPublishExtension> {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("OSSRH_USERNAME"))
            password.set(System.getenv("OSSRH_PASSWORD"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.elebras1"
            artifactId = "flecs-java"
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set("Flecs Java")
                description.set("Java wrapper for the ECS library Flecs using the Java Foreign Function & Memory API (FFM)")
                url.set("https://github.com/elebras1/flecs-java")

                withXml {
                    val node = asNode()
                    val dependenciesNodes = node.get("dependencies") as groovy.util.NodeList
                    if (dependenciesNodes.isNotEmpty()) {
                        node.remove(dependenciesNodes[0] as groovy.util.Node)
                    }
                }

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/elebras1/flecs-java/blob/main/LICENSE")
                    }
                }

                developers {
                    developer {
                        id.set("elebras1")
                        name.set("elebras1")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/elebras1/flecs-java.git")
                    developerConnection.set("scm:git:ssh://github.com/elebras1/flecs-java.git")
                    url.set("https://github.com/elebras1/flecs-java")
                }
            }
        }
    }
}

configure<SigningExtension> {
    val signingKey = System.getenv("SIGNING_KEY")
    val signingPassword = System.getenv("SIGNING_PASSWORD")
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["maven"])
    }
}

extra["flecsVersion"] = flecsVersion
extra["flecsIncludeDir"] = File(flecsSourceDir, "include")
extra["flecsLibDir"] = layout.buildDirectory.dir("resources/main/natives/$nativeArch").get().asFile