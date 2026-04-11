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
    jvmArgs("--enable-native-access=ALL-UNNAMED", "--add-modules", "jdk.incubator.vector")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf(
        "--add-modules", "jdk.incubator.vector"
    ))

    val processorOutput = rootProject.layout.buildDirectory.dir("classes/java/processor").get().asFile
    options.annotationProcessorPath = files(processorOutput) + rootProject.configurations.runtimeClasspath.get()
}
