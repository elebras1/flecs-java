plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(rootProject)
    annotationProcessor(project(":"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

val localPlatformId: String = rootProject.findProperty("NATIVE_ARCH") as String?
    ?: run {
        val os   = org.gradle.internal.os.OperatingSystem.current()
        val arch = System.getProperty("os.arch").lowercase()
        when {
            os.isLinux && arch in listOf("amd64", "x86_64") -> "linux-x64"
            os.isLinux && arch in listOf("aarch64", "arm64") -> "linux-aarch64"
            os.isWindows && arch in listOf("amd64", "x86_64") -> "windows-x64"
            os.isMacOsX && arch in listOf("aarch64", "arm64") -> "macos-aarch64"
            os.isMacOsX && arch in listOf("amd64", "x86_64") -> "macos-x64"
            else -> "linux-x64"
        }
    }

val libName: String = when {
    org.gradle.internal.os.OperatingSystem.current().isWindows -> "flecs.dll"
    org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "libflecs.dylib"
    else -> "libflecs.so"
}

val nativePath: String = rootProject.layout.buildDirectory
    .file("natives/$localPlatformId/release/$libName")
    .get().asFile.absolutePath

tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    systemProperty("flecs.native.path", nativePath)
    dependsOn(":compileFlecsNative-$localPlatformId-release")
}

tasks.withType<JavaCompile> {
    val processorOutput = rootProject.layout.buildDirectory.dir("classes/java/processor").get().asFile
    options.annotationProcessorPath = files(processorOutput) + rootProject.configurations.runtimeClasspath.get()
}