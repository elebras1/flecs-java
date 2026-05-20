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
        languageVersion.set(JavaLanguageVersion.of(27))
    }
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED", "--enable-preview")
}

tasks.withType<JavaCompile> {
    val processorOutput = rootProject.layout.buildDirectory.dir("classes/java/processor").get().asFile
    options.annotationProcessorPath = files(processorOutput) + rootProject.configurations.runtimeClasspath.get()
    options.compilerArgs.add("--enable-preview")
}
