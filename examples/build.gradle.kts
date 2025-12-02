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

tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.compileJava {
    val processorOutput = rootProject.layout.buildDirectory.dir("classes/java/processor").get().asFile
    options.annotationProcessorPath = files(processorOutput) + rootProject.configurations.runtimeClasspath.get()
}
