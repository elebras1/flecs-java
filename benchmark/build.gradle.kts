import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("java")
    id("me.champeau.jmh") version "0.7.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(rootProject)
    annotationProcessor(project(":"))
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

val flecsVersion: String by rootProject.extra
val flecsIncludeDir: File by rootProject.extra
val flecsLibDir: File by rootProject.extra

val benchmarkBinary = layout.buildDirectory.file("bench_native/bench").get().asFile
val jmhResultFile = layout.buildDirectory.file("results/jmh/results.txt").get().asFile
val cResultFile = layout.buildDirectory.file("results/c/results.txt").get().asFile
val reportFile = file("results/benchmark-results.txt")

jmh {
    fork.set(1)
    warmupIterations.set(3)
    iterations.set(3)
    timeOnIteration.set("1s")
    profilers.add("gc")
    warmupBatchSize.set(5)
    jvmArgs.set(listOf("--enable-native-access=ALL-UNNAMED"))
    failOnError.set(true)
    resultFormat.set("TEXT")
    resultsFile.set(jmhResultFile)
}

val compileCBenchmark by tasks.registering(Exec::class) {
    group = "benchmark"
    dependsOn(rootProject.tasks.getByPath(":compileFlecsNative"))

    val sourceFile = file("src/jmh/c/benchmark.c")
    inputs.file(sourceFile)
    inputs.dir(flecsIncludeDir)
    inputs.dir(flecsLibDir)
    outputs.file(benchmarkBinary)

    doFirst { benchmarkBinary.parentFile.mkdirs() }

    commandLine(
        "gcc", sourceFile.absolutePath,
        "-o", benchmarkBinary.absolutePath,
        "-I", flecsIncludeDir.absolutePath,
        "-L", flecsLibDir.absolutePath,
        "-lflecs", "-O3", "-march=native", "-lm", "-lpthread"
    )
}

val runCBenchmark by tasks.registering(Exec::class) {
    group = "benchmark"
    dependsOn(compileCBenchmark)
    executable = benchmarkBinary.absolutePath
    environment("LD_LIBRARY_PATH", flecsLibDir.absolutePath)
    outputs.upToDateWhen { false }

    doFirst {
        cResultFile.parentFile.mkdirs()
        standardOutput = cResultFile.outputStream()
    }
}

fun machineInfo(): String {
    fun cmd(vararg args: String) = runCatching {
        ProcessBuilder(*args).start().inputStream.bufferedReader().readText().trim()
    }.getOrDefault("unknown")

    val cpu   = cmd("sh", "-c", "grep 'model name' /proc/cpuinfo | head -1 | cut -d: -f2").trim()
    val cores = Runtime.getRuntime().availableProcessors()
    val ramKb = cmd("sh", "-c", "grep MemTotal /proc/meminfo | awk '{print \$2}'").toLongOrNull() ?: 0
    val ramGb = ramKb / 1024 / 1024
    val os    = cmd("sh", "-c", "uname -sr")
    val kernel= cmd("sh", "-c", "uname -r")
    val jvm   = "${System.getProperty("java.vendor")} ${System.getProperty("java.version")}"
    val gcc   = cmd("sh", "-c", "gcc --version | head -1")

    return """
        |Machine Configuration
        |${"=".repeat(60)}
        |OS      : $os
        |Kernel  : $kernel
        |CPU     : $cpu
        |Cores   : $cores
        |RAM     : $ramGb GB
        |JVM     : $jvm
        |GCC     : $gcc
        |${"=".repeat(60)}
    """.trimMargin()
}

val mergeBenchmarkResults by tasks.registering {
    group = "benchmark"
    dependsOn("jmh", runCBenchmark)

    doLast {
        reportFile.parentFile.mkdirs()
        val sb = StringBuilder()

        sb.appendLine(machineInfo())
        sb.appendLine()
        sb.appendLine("BENCHMARK RESULTS — flecs-java vs flecs C")
        sb.appendLine("Date : ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        sb.appendLine()

        sb.appendLine("[ Java / JMH ]")
        sb.appendLine("-".repeat(60))
        if (jmhResultFile.exists()) {
            val lines = jmhResultFile.readLines()
            val tableStart = lines.indexOfLast { it.trimStart().startsWith("Benchmark") }
            if (tableStart >= 0) {
                lines.drop(tableStart).forEach { sb.appendLine(it) }
            }
        }

        sb.appendLine()
        sb.appendLine("[ C / Native ]")
        sb.appendLine("-".repeat(60))
        if (cResultFile.exists()) sb.append(cResultFile.readText())

        reportFile.writeText(sb.toString())
        println("✅ ${reportFile.absolutePath}")
    }
}

tasks.named("jmh") {
    outputs.upToDateWhen { false }
}

val benchmarkAll by tasks.registering {
    group = "benchmark"
    dependsOn(mergeBenchmarkResults)
}