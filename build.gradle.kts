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

val os: OperatingSystem = OperatingSystem.current()
val userHome: String = System.getProperty("user.home")

val jextractExecutable = when {
    os.isWindows -> "$userHome/.local/jextract-25/bin/jextract.exe"
    else -> "$userHome/.local/jextract-25/bin/jextract"
}

data class Platform(
    val id: String,
    val osFamily: String,
    val archName: String,
    val libName: String,
    val compilerFlags: List<String>,
    val linkerFlags: List<String>,
    val extraDefinitions: List<String> = emptyList(),
    val extraLibs: List<String> = emptyList()
)

val platforms = listOf(
    Platform(
        id = "linux-x64",
        osFamily = "linux",
        archName = "x86-64",
        libName = "libflecs.so",
        compilerFlags = listOf("-march=x86-64-v2", "-Ofast", "-flto", "-fomit-frame-pointer", "-funroll-loops", "-fno-semantic-interposition", "-fno-plt"),
        linkerFlags  = listOf("-shared", "-fPIC"),
        extraDefinitions = listOf("-D_POSIX_C_SOURCE=200809L", "-D_DEFAULT_SOURCE"),
        extraLibs = listOf("-lm", "-lrt", "-lpthread")
    ),
    Platform(
        id = "linux-aarch64",
        osFamily = "linux",
        archName = "aarch64",
        libName = "libflecs.so",
        compilerFlags = listOf("-march=armv8-a", "-Ofast", "-flto", "-fomit-frame-pointer", "-funroll-loops"),
        linkerFlags  = listOf("-shared", "-fPIC"),
        extraDefinitions = listOf("-D_POSIX_C_SOURCE=200809L", "-D_DEFAULT_SOURCE"),
        extraLibs = listOf("-lm", "-lrt", "-lpthread")
    ),
    Platform(
        id = "windows-x64",
        osFamily = "windows",
        archName = "x86-64",
        libName = "flecs.dll",
        compilerFlags = listOf("-march=x86-64-v2", "-Ofast", "-flto", "-fomit-frame-pointer", "-funroll-loops"),
        linkerFlags  = listOf("-shared"),
        extraLibs = listOf("-lws2_32", "-ldbghelp")
    ),
    Platform(
        id = "macos-x64",
        osFamily = "macos",
        archName = "x86-64",
        libName = "libflecs.dylib",
        compilerFlags = listOf("-mtune=generic", "-Ofast", "-flto", "-fomit-frame-pointer", "-funroll-loops"),
        linkerFlags  = listOf("-dynamiclib"),
        extraLibs = listOf("-framework", "CoreFoundation")
    ),
    Platform(
        id = "macos-aarch64",
        osFamily = "macos",
        archName = "aarch64",
        libName = "libflecs.dylib",
        compilerFlags = listOf("-march=armv8-a", "-Ofast", "-flto", "-fomit-frame-pointer", "-funroll-loops"),
        linkerFlags  = listOf("-dynamiclib"),
        extraLibs = listOf("-framework", "CoreFoundation")
    )
)

val localPlatformId = project.findProperty("NATIVE_ARCH") as String? ?: when {
    os.isLinux && System.getProperty("os.arch") in listOf("amd64", "x86_64") -> "linux-x64"
    os.isLinux && System.getProperty("os.arch") in listOf("aarch64", "arm64") -> "linux-aarch64"
    os.isWindows && System.getProperty("os.arch") in listOf("amd64", "x86_64") -> "windows-x64"
    os.isMacOsX && System.getProperty("os.arch") in listOf("aarch64", "arm64") -> "macos-aarch64"
    os.isMacOsX && System.getProperty("os.arch") in listOf("amd64", "x86_64") -> "macos-x64"
    else -> "linux-x64"
}

val downloadFlecs by tasks.registering {
    description = "Download Flecs release from GitHub"
    group = "flecs"

    val outputFile = File(flecsDir, "flecs-$flecsVersion.tar.gz")
    outputs.file(outputFile)

    doLast {
        flecsDir.mkdirs()
        val url = "https://github.com/SanderMertens/flecs/archive/refs/tags/v$flecsVersion.tar.gz"
        println("Downloading Flecs $flecsVersion from $url")
        URI(url).toURL().openConnection().getInputStream().use { input ->
            Files.copy(input, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}

val extractFlecs by tasks.registering(Copy::class) {
    description = "Extract Flecs archive"
    group = "flecs"
    dependsOn(downloadFlecs)
    from(tarTree(downloadFlecs.get().outputs.files.singleFile))
    into(flecsDir)
}

fun nativeOutputDir(platformId: String, variant: String) = layout.buildDirectory.dir("natives/$platformId/$variant").get().asFile

fun compileNativeTask(platform: Platform, debug: Boolean): TaskProvider<Exec> {
    val variant  = if (debug) "debug" else "release"
    val taskName = "compileFlecsNative-${platform.id}-$variant"

    tasks.findByName(taskName)?.let {
        return tasks.named<Exec>(taskName)
    }

    val outputDir = nativeOutputDir(platform.id, variant)
    val outputLib = File(outputDir, platform.libName)

    return tasks.register<Exec>(taskName) {
        description = "Compile Flecs native ($variant) for ${platform.id}"
        group = "flecs"
        dependsOn(extractFlecs)

        workingDir(flecsSourceDir)
        outputs.file(outputLib)
        doFirst { outputDir.mkdirs() }

        val debugFlags = if (debug)
            listOf("-DFLECS_DEBUG", "-DFLECS_SANITIZE", "-g", "-O0")
        else
            listOf("-DNDEBUG")

        val compilerTarget = project.findProperty("COMPILER_TARGET") as String?

        commandLine(buildList {
            add("gcc")
            addAll(platform.linkerFlags)
            add("-o"); add(outputLib.absolutePath)
            add(flecsCFile.absolutePath)
            addAll(platform.compilerFlags)
            addAll(debugFlags)
            if (!compilerTarget.isNullOrEmpty()) {
                add("-target"); add(compilerTarget)
            }
            add("-std=c99")
            add("-Dflecs_EXPORTS")
            addAll(platform.extraDefinitions)
            addAll(platform.extraLibs)
        })
    }
}

val skipNative = providers.gradleProperty("skipNative")

tasks.configureEach {
    if (name.startsWith("compileFlecsNative") ||
        name == "downloadFlecs" ||
        name == "extractFlecs") {
        onlyIf("skipNative property not set") { !skipNative.isPresent }
    }
}

val compileFlecsNative by tasks.registering {
    description = "Compile Flecs native for the current platform (release)"
    group = "flecs"
    val localPlatform = platforms.first { it.id == localPlatformId }
    dependsOn(compileNativeTask(localPlatform, debug = false))
}

data class NativeJarSpec(
    val platform: Platform,
    val debug: Boolean,
    val compileTask: TaskProvider<Exec>
)

val nativeJarSpecs = platforms.flatMap { platform ->
    listOf(false, true).map { debug ->
        val compileTask = compileNativeTask(platform, debug)
        NativeJarSpec(platform, debug, compileTask)
    }
}

val nativeJarTasks = nativeJarSpecs.map { spec ->
    val variant  = if (spec.debug) "debug" else "release"
    val taskName = "jarNatives-${spec.platform.id}-$variant"

    val jarTask = tasks.register<Jar>(taskName) {
        description = "Package Flecs natives (${spec.platform.id}, $variant)"
        group = "flecs"
        dependsOn(spec.compileTask)

        from(nativeOutputDir(spec.platform.id, variant)) {
            into("natives/${spec.platform.id}")
        }

        archiveClassifier.set(
            if (spec.debug) {
                "natives-${spec.platform.id}-debug"
            } else {
                "natives-${spec.platform.id}"
            }
        )
    }

    Triple(spec, jarTask, variant)
}

val nativeVariantConfigurations = nativeJarTasks.map { (spec, jarTask, variant) ->
    val cfgName = "natives-${spec.platform.id}-$variant"
    val cfg = configurations.create(cfgName) {
        isCanBeConsumed = true
        isCanBeResolved = false

        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class, Usage.JAVA_RUNTIME))
            attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objects.named(OperatingSystemFamily::class, spec.platform.osFamily))
            attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(MachineArchitecture::class, spec.platform.archName))
            attribute(Attribute.of("io.github.elebras1.flecs.variant", String::class.java), variant)
        }
        outgoing.artifact(jarTask)
    }
    Triple(spec, jarTask, cfg)
}

tasks.jar {
    dependsOn(compileProcessor, copyProcessorResources)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(compileProcessor.get().destinationDirectory)
    from(copyProcessorResources.get().destinationDir)

    exclude("natives/**")

    manifest {
        attributes(
            "Implementation-Title" to "Flecs Java",
            "Implementation-Version" to project.version
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId    = "io.github.elebras1"
            artifactId = "flecs-java"
            version    = project.version.toString()

            from(components["java"])

            nativeJarTasks.forEach { (spec, jarTask, variant) ->
                artifact(jarTask)
            }

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

val generateFlecsBindings by tasks.registering(Exec::class) {
    description = "Generate Java FFM bindings using jextract"
    group = "flecs"
    dependsOn(extractFlecs)
    inputs.file(flecsHeaderFile)
    outputs.dir(generatedSourcesDir)

    doFirst {
        if (!flecsHeaderFile.exists())
            throw GradleException("Flecs header not found: ${flecsHeaderFile.absolutePath}")
        generatedSourcesDir.mkdirs()
    }

    commandLine(
        jextractExecutable,
        "--output", generatedSourcesDir.absolutePath,
        "-t", "io.github.elebras1.flecs",
        "-I", File(flecsSourceDir, "distr").absolutePath,
        flecsHeaderFile.absolutePath
    )
}

val validateGeneratedBindings by tasks.registering {
    group = "verification"
    doLast {
        if (!generatedSourcesDir.exists())
            throw GradleException("Bindings not found at: $generatedSourcesDir")
    }
}

val generatorSourcesDir = file("src/generator/java")

val generateEachBase by tasks.registering(JavaExec::class) {
    description = "Generate typed each callback interfaces"
    group = "flecs"
    classpath = sourceSets["generator"].runtimeClasspath
    mainClass.set("io.github.elebras1.flecs.EachBaseGenerator")
    args(generatedSourcesDir.absolutePath)
    outputs.dir(generatedSourcesDir)
    doFirst { generatedSourcesDir.mkdirs() }
}

sourceSets {
    create("generator") {
        java.srcDir(generatorSourcesDir)
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
    main {
        java {
            srcDir(generatedSourcesDir)
            srcDir(annotationGeneratedMainDir)
        }
    }
    test {
        java { srcDir(annotationGeneratedTestDir) }
    }
}

configurations["generatorCompileClasspath"].extendsFrom(configurations["compileClasspath"])
configurations["generatorRuntimeClasspath"].extendsFrom(configurations["runtimeClasspath"])

val compileProcessor by tasks.registering(JavaCompile::class) {
    source = fileTree("src/main/java") {
        include("**/processor/**")
        include("**/annotation/**")
        include("**/util/internal/codegen/**")
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
    options.compilerArgs.addAll(listOf("-s", annotationGeneratedMainDir.absolutePath))

    doFirst {
        annotationGeneratedMainDir.deleteRecursively()
        annotationGeneratedMainDir.mkdirs()
    }
}

tasks.compileTestJava {
    dependsOn(compileProcessor)
    val processorOutput = compileProcessor.get().destinationDirectory.get().asFile
    classpath = files(processorOutput) + classpath
    options.annotationProcessorPath = files(processorOutput) + configurations.runtimeClasspath.get()
    options.compilerArgs.addAll(listOf("-s", annotationGeneratedTestDir.absolutePath))

    doFirst {
        annotationGeneratedTestDir.deleteRecursively()
        annotationGeneratedTestDir.mkdirs()
    }
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED")

    val localPlatform = platforms.first { it.id == localPlatformId }
    val nativeDir = nativeOutputDir(localPlatformId, "release")
    systemProperty("flecs.native.path", File(nativeDir, localPlatform.libName).absolutePath)

    dependsOn(compileNativeTask(localPlatform, debug = false))
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

val cleanFlecs by tasks.registering(Delete::class) {
    description = "Delete Flecs downloaded, compiled and generated files"
    group = "flecs"
    delete(flecsDir)
}

tasks.clean {
    dependsOn(cleanFlecs)
}

extra["flecsVersion"] = flecsVersion
extra["flecsIncludeDir"] = File(flecsSourceDir, "include")