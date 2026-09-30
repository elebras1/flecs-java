import org.gradle.internal.os.OperatingSystem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    implementation("net.onedaybeard.artemis:artemis-odb:2.3.0")
    implementation("dev.dominion.ecs:dominion-ecs-engine:0.9.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

val os: OperatingSystem = OperatingSystem.current()
val osIsWindows: Boolean = os.isWindows

val flecsVersion: String by rootProject.extra
val flecsIncludeDir: File by rootProject.extra
val archFlag: String by rootProject.extra

val flecsSourceDir: File = flecsIncludeDir.parentFile
val flecsCFile: File = File(flecsSourceDir, "distr/flecs.c")

val artifactVersions = linkedMapOf(
    "flecs C" to flecsVersion,
    "flecs-java" to rootProject.version.toString(),
    "artemis-odb" to "2.3.0",
    "dominion-ecs" to "0.9.0"
)

val benchBuildDir = layout.buildDirectory.dir("bench_native").get().asFile
val benchmarkBinary = benchBuildDir.resolve("bench${if (osIsWindows) ".exe" else ""}")
val jmhResultFile = layout.buildDirectory.file("results/jmh/results.csv").get().asFile
val cResultFile = layout.buildDirectory.file("results/c/results.txt").get().asFile
val reportDir = file("results")
val markdownReportFile = File(reportDir, "benchmark-results.md")
val csvReportFile = File(reportDir, "benchmark-results.csv")

val benchQuick = providers.gradleProperty("quick").isPresent

jmh {
    jmhVersion.set("1.37")
    fork.set(1)
    warmupIterations.set(if (benchQuick) 1 else 3)
    iterations.set(if (benchQuick) 2 else 3)
    timeOnIteration.set("1s")
    profilers.add("gc")
    warmupBatchSize.set(5)
    jvmArgs.set(listOf(
        "--enable-native-access=ALL-UNNAMED",
        "-Duser.language=en",
        "-Duser.country=US"
    ))
    failOnError.set(true)
    resultFormat.set("CSV")
    resultsFile.set(jmhResultFile)
    providers.gradleProperty("benchInclude").orNull?.let { includes.add(it) }
}

tasks.named("jmh") {
    outputs.upToDateWhen { false }
}

val cSrcDir = file("src/jmh/c")

val cSources = listOf(
    "main.c",
    "benchmark_utils.c",
    "tier1_benchmarks.c",
    "tier2_benchmarks.c",
)

val flecsObjectFile = benchBuildDir.resolve("flecs.o")

val generateClangd by tasks.registering {
    group = "benchmark"
    dependsOn(rootProject.tasks.getByPath(":compileFlecsNative"))

    val clangdFile = file(".clangd")
    outputs.file(clangdFile)

    doLast {
        clangdFile.writeText("""
            CompileFlags:
              Add:
                - -I${cSrcDir.absolutePath}
                - -I${flecsIncludeDir.absolutePath}
                - -Dflecs_STATIC
                - -DNDEBUG
                - -std=c11
        """.trimIndent())
    }
}

val compileObjects by tasks.registering {
    group = "benchmark"
    dependsOn(rootProject.tasks.getByPath(":compileFlecsNative"), generateClangd)

    inputs.dir(cSrcDir)
    inputs.dir(flecsIncludeDir)
    inputs.property("quick", benchQuick)
    inputs.file(flecsCFile)
    outputs.dir(benchBuildDir)

    doFirst { benchBuildDir.mkdirs() }

    doLast {
        check(flecsCFile.exists()) {
            "Flecs amalgamation not found at ${flecsCFile.absolutePath}"
        }

        fun runGcc(args: List<String>) {
            val exitCode = ProcessBuilder(args).inheritIO().start().waitFor()
            check(exitCode == 0) {
                "gcc failed (exit code $exitCode): ${args.joinToString(" ")}"
            }
        }
        val commonFlags = mutableListOf(
            "-Ofast",
            "-flto",
            "-fomit-frame-pointer",
            "-funroll-loops",
            "-DNDEBUG",
            archFlag,
        )
        if (!osIsWindows && !os.isMacOsX) {
            commonFlags.addAll(listOf("-fno-semantic-interposition", "-fno-plt"))
        }

        cSources.forEach { src ->
            val srcFile = cSrcDir.resolve(src)
            val objFile = benchBuildDir.resolve(src.replace(".c", ".o"))
            val cmd = mutableListOf(
                "gcc",
                "-c", srcFile.absolutePath,
                "-o", objFile.absolutePath,
                "-I", cSrcDir.absolutePath,
                "-I", flecsIncludeDir.absolutePath,
                "-Dflecs_STATIC",
                "-std=c11",
            )
            cmd.addAll(commonFlags)
            if (benchQuick) {
                cmd.add("-DBENCH_QUICK")
            }
            runGcc(cmd)
        }

        val flecsCmd = mutableListOf(
            "gcc",
            "-c", flecsCFile.absolutePath,
            "-o", flecsObjectFile.absolutePath,
            "-DFLECS_STATIC",
            "-Dflecs_STATIC",
            "-std=c99",
        )
        if (!osIsWindows) {
            flecsCmd.addAll(listOf(
                "-D_POSIX_C_SOURCE=200809L",
                "-D_DEFAULT_SOURCE",
            ))
        }
        flecsCmd.addAll(commonFlags)
        runGcc(flecsCmd)
    }
}

val compileCBenchmark by tasks.registering(Exec::class) {
    group = "benchmark"
    dependsOn(compileObjects)

    val objFiles = cSources.map { benchBuildDir.resolve(it.replace(".c", ".o")) } +
        flecsObjectFile
    inputs.files(objFiles)
    outputs.file(benchmarkBinary)

    commandLine(buildList {
        add("gcc")
        addAll(objFiles.map { it.absolutePath })
        add("-o"); add(benchmarkBinary.absolutePath)
        add("-flto")
        if (osIsWindows) {
            add("-lws2_32")
            add("-ldbghelp")
        } else {
            add("-lm")
            add("-lpthread")
        }
    })
}

val runCBenchmark by tasks.registering(Exec::class) {
    group = "benchmark"
    dependsOn(compileCBenchmark)
    executable = benchmarkBinary.absolutePath
    outputs.upToDateWhen { false }

    doFirst {
        cResultFile.parentFile.mkdirs()
        standardOutput = cResultFile.outputStream()
    }
}
data class BenchRow(
    val tier: Int,
    val benchmark: String,
    val impl: String,
    val n: Int,
    val nsPerUnit: Double,
)

val tier1Benchmarks = listOf(
    "createEmpty", "create1", "create2", "createFromPrefab",
    "destroyEmpty", "destroy2", "addComponent", "removeComponent",
    "get", "getSet", "has", "query1Read", "query2ReadWrite", "queryFiltered",
    "systemRun", "queryCreate", "lookup", "mixedSimulation"
)

val tier2Benchmarks = listOf(
    "pairAdd", "pairIterate", "hierarchyBuild", "hierarchyTraverse",
    "prefabInheritGet", "observerAdd", "singletonGetSet", "deferAdd",
    "multiThreadedProgress"
)

val benchmarkTier = (tier1Benchmarks.map { it to 1 } + tier2Benchmarks.map { it to 2 }).toMap()

val benchmarkUnits = mapOf(
    "createEmpty" to "ns/entity", "create1" to "ns/entity", "create2" to "ns/entity",
    "createFromPrefab" to "ns/entity", "destroyEmpty" to "ns/entity", "destroy2" to "ns/entity",
    "addComponent" to "ns/entity", "removeComponent" to "ns/entity",
    "get" to "ns/op", "getSet" to "ns/op", "has" to "ns/op",
    "query1Read" to "ns/entity", "query2ReadWrite" to "ns/entity", "queryFiltered" to "ns/entity",
    "systemRun" to "ns/entity", "queryCreate" to "ns/op", "lookup" to "ns/op",
    "mixedSimulation" to "ns/entity", "pairAdd" to "ns/entity", "pairIterate" to "ns/entity",
    "hierarchyBuild" to "ns/entity", "hierarchyTraverse" to "ns/entity",
    "prefabInheritGet" to "ns/op", "observerAdd" to "ns/entity", "singletonGetSet" to "ns/op",
    "deferAdd" to "ns/entity", "multiThreadedProgress" to "ns/entity"
)

val implOrder = listOf("flecs-c", "flecs-java", "artemis-odb", "dominion-ecs")
val entityCounts = listOf(1000, 10000, 100000)

fun benchTierOf(benchmark: String): Int = benchmarkTier[benchmark] ?: 1

fun benchmarkNameOf(benchmark: String): String? {
    val simple = benchmark.substringAfterLast('.').replace(Regex("^s\\d{2}_"), "")
    return if (benchmarkTier.containsKey(simple)) simple else null
}

fun benchImplOf(benchmark: String): String? {
    val lower = benchmark.lowercase(Locale.US)
    return when {
        lower.contains("dominion") -> "dominion-ecs"
        lower.contains("artemis") -> "artemis-odb"
        lower.contains("flecs") -> "flecs-java"
        else -> null
    }
}

fun benchToNanos(score: Double, unit: String): Double {
    val normalized = unit.replace('µ', 'u').replace('μ', 'u').trim().lowercase(Locale.US)
    val factor = when {
        normalized.startsWith("ns") -> 1.0
        normalized.startsWith("us") -> 1_000.0
        normalized.startsWith("ms") -> 1_000_000.0
        normalized.startsWith("s") -> 1_000_000_000.0
        normalized.startsWith("m") -> 60_000_000_000.0
        else -> 1.0
    }
    return score * factor
}

fun parseCsvLine(line: String): List<String> {
    val cells = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val ch = line[i]
        when {
            ch == '"' -> {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    current.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            }
            ch == ',' && !inQuotes -> {
                cells.add(current.toString())
                current.setLength(0)
            }
            else -> current.append(ch)
        }
        i++
    }
    cells.add(current.toString())
    return cells
}

fun benchParseDouble(text: String?): Double? {
    val cleaned = text?.trim() ?: return null
    return cleaned.toDoubleOrNull() ?: cleaned.replace(',', '.').toDoubleOrNull()
}

fun parseJmhResults(file: File): List<BenchRow> {
    if (!file.exists()) return emptyList()
    val rows = mutableListOf<BenchRow>()
    var header: List<String>? = null

    file.readLines().forEach { rawLine ->
        val line = rawLine.trim()
        if (line.isEmpty()) return@forEach
        val cells = parseCsvLine(line)
        if (header == null) {
            if (cells.any { it.equals("Benchmark", ignoreCase = true) }) {
                header = cells
            }
            return@forEach
        }

        val columns = header!!
        val benchmarkFqn = cells.getOrNull(
            columns.indexOfFirst { it.equals("Benchmark", ignoreCase = true) }
        ) ?: return@forEach
        if (benchmarkFqn.contains(':')) return@forEach
        val benchmark = benchmarkNameOf(benchmarkFqn) ?: return@forEach
        val impl = benchImplOf(benchmarkFqn) ?: return@forEach
        val score = benchParseDouble(
            cells.getOrNull(
                columns.indexOfFirst { it.equals("Score", ignoreCase = true) }
            )
        ) ?: return@forEach
        val unit = cells.getOrNull(
            columns.indexOfFirst { it.equals("Unit", ignoreCase = true) }
        ) ?: "ns/op"
        val n = columns.mapIndexedNotNull { index, name ->
            if (name.startsWith("Param:", ignoreCase = true)) {
                cells.getOrNull(index)?.trim()?.toIntOrNull()
            } else {
                null
            }
        }.firstOrNull { it > 0 } ?: return@forEach
        rows.add(
            BenchRow(
                benchTierOf(benchmark), benchmark, impl, n,
                benchToNanos(score, unit) / n
            )
        )
    }
    return rows
}

fun parseCResults(file: File): List<BenchRow> {
    if (!file.exists()) return emptyList()
    val pattern = Regex("^RESULT\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s*$")
    val rows = mutableListOf<BenchRow>()
    file.readLines().forEach { rawLine ->
        val match = pattern.find(rawLine.trim()) ?: return@forEach
        val benchmark = match.groupValues[1]
        val impl = match.groupValues[2]
        val n = match.groupValues[3].toIntOrNull() ?: return@forEach
        val value = match.groupValues[4].toDoubleOrNull() ?: return@forEach
        rows.add(BenchRow(benchTierOf(benchmark), benchmark, impl, n, value))
    }
    return rows
}

fun benchCommand(vararg args: String): String = runCatching {
    val process = ProcessBuilder(*args).redirectErrorStream(true).start()
    val output = process.inputStream.bufferedReader().readText().trim()
    process.waitFor()
    output
}.getOrDefault("unknown")

fun machineInfoMap(): LinkedHashMap<String, String> {
    val cpu = if (osIsWindows) {
        benchCommand("powershell", "-Command", "(Get-CimInstance Win32_Processor).Name")
    } else {
        benchCommand("sh", "-c", "grep 'model name' /proc/cpuinfo | head -1 | cut -d: -f2").trim()
    }

    val ramGb = if (osIsWindows) {
        val bytes = benchCommand(
            "powershell", "-Command",
            "(Get-CimInstance Win32_ComputerSystem).TotalPhysicalMemory"
        ).toLongOrNull() ?: 0L
        bytes / 1024 / 1024 / 1024
    } else {
        val kb = benchCommand(
            "sh", "-c", "grep MemTotal /proc/meminfo | awk '{print \$2}'"
        ).toLongOrNull() ?: 0L
        kb / 1024 / 1024
    }

    val info = LinkedHashMap<String, String>()
    info["os"] = if (osIsWindows) {
        benchCommand("powershell", "-Command", "(Get-CimInstance Win32_OperatingSystem).Caption")
    } else {
        benchCommand("sh", "-c", "uname -sr")
    }
    info["kernel"] = if (osIsWindows) {
        benchCommand("powershell", "-Command", "(Get-CimInstance Win32_OperatingSystem).Version")
    } else {
        benchCommand("sh", "-c", "uname -r")
    }
    info["cpu"] = cpu
    info["cores"] = Runtime.getRuntime().availableProcessors().toString()
    info["ram_gb"] = ramGb.toString()
    info["jvm"] = "${System.getProperty("java.vendor")} ${System.getProperty("java.version")}"
    info["gcc"] = if (osIsWindows) {
        benchCommand("gcc", "--version")
    } else {
        benchCommand("sh", "-c", "gcc --version | head -1")
    }
    return info
}

fun gitCommitHash(): String = runCatching {
    val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
        .directory(rootProject.projectDir)
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().readText().trim()
    process.waitFor()
    if (output.isEmpty()) "unknown" else output
}.getOrDefault("unknown")

fun formatNumber(value: Double): String = String.format(Locale.US, "%.3f", value)

fun formatRatio(value: Double): String = String.format(Locale.US, "%.2f", value)

val mergeBenchmarkResults by tasks.registering {
    group = "benchmark"
    dependsOn("jmh", runCBenchmark)

    doLast {
        reportDir.mkdirs()

        val combined = parseJmhResults(jmhResultFile) + parseCResults(cResultFile)
        val deduplicated = LinkedHashMap<String, BenchRow>()
        combined.forEach { row ->
            deduplicated.putIfAbsent(
                "${row.tier}|${row.benchmark}|${row.n}|${row.impl}", row
            )
        }

        val values = HashMap<String, Double>()
        deduplicated.values.forEach { row ->
            values["${row.tier}|${row.benchmark}|${row.n}|${row.impl}"] = row.nsPerUnit
        }

        fun valueOf(tier: Int, benchmark: String, n: Int, impl: String): Double? =
            values["$tier|$benchmark|$n|$impl"]
        val flecsBenchmarks = tier1Benchmarks + tier2Benchmarks
        val otherBenchmarks = tier1Benchmarks.filter { it != "queryCreate" && it != "lookup" }
        val requiredBenchmarks = linkedMapOf(
            "flecs-c" to flecsBenchmarks,
            "flecs-java" to flecsBenchmarks,
            "artemis-odb" to otherBenchmarks,
            "dominion-ecs" to otherBenchmarks
        )
        val missingResults = mutableListOf<String>()
        requiredBenchmarks.forEach { (impl, benchmarks) ->
            benchmarks.forEach { benchmark ->
                entityCounts.forEach { n ->
                    if (valueOf(benchTierOf(benchmark), benchmark, n, impl) == null) {
                        missingResults.add("$impl $benchmark n=$n")
                    }
                }
            }
        }
        check(missingResults.isEmpty()) {
            "Benchmark results are missing (benchmark probably failed and was dropped):\n  " +
                missingResults.joinToString("\n  ")
        }

        fun cell(tier: Int, benchmark: String, n: Int, impl: String): String {
            val value = valueOf(tier, benchmark, n, impl) ?: return "n/a"
            val baseline = valueOf(tier, benchmark, n, "flecs-c")
            return if (baseline != null && baseline > 0.0) {
                "${formatNumber(value)} (${formatRatio(value / baseline)}x)"
            } else {
                formatNumber(value)
            }
        }

        fun javaRatio(tier: Int, benchmark: String, n: Int): String {
            val javaValue = valueOf(tier, benchmark, n, "flecs-java") ?: return "n/a"
            val cValue = valueOf(tier, benchmark, n, "flecs-c") ?: return "n/a"
            if (cValue <= 0.0) return "n/a"
            return formatRatio(javaValue / cValue)
        }

        val machineInfo = machineInfoMap()
        val generated = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val commit = gitCommitHash()

        val markdown = StringBuilder()
        markdown.appendLine("# Benchmark results")
        markdown.appendLine()
        markdown.appendLine("Generated: $generated  ")
        markdown.appendLine("Git commit: `$commit`  ")
        markdown.appendLine("Mode: ${if (benchQuick) "**QUICK (reduced warmup/iterations, draft)**" else "full"}")
        markdown.appendLine()
        if (benchQuick) {
            markdown.appendLine("> **Draft run.** Generated with `-Pquick`: fewer warmup and measurement")
            markdown.appendLine("> iterations. Use it to validate the harness, not to compare numbers;")
            markdown.appendLine("> rerun without `-Pquick` for publishable results.")
            markdown.appendLine()
        }
        markdown.appendLine("## Machine")
        markdown.appendLine()
        markdown.appendLine("| Key | Value |")
        markdown.appendLine("|---|---|")
        machineInfo.forEach { (key, value) ->
            markdown.appendLine("| $key | $value |")
        }
        markdown.appendLine()
        markdown.appendLine("## Library versions")
        markdown.appendLine()
        markdown.appendLine("| Library | Version |")
        markdown.appendLine("|---|---|")
        artifactVersions.forEach { (library, version) ->
            markdown.appendLine("| $library | $version |")
        }
        markdown.appendLine()
        markdown.appendLine("All values in the tables below are ns per unit (JMH average for Java,")
        markdown.appendLine("median of the measured runs for C). The value in parentheses is the")
        markdown.appendLine("ratio versus flecs C (lower is faster). Tier 1 compares flecs-java,")
        markdown.appendLine("artemis-odb and dominion-ecs against flecs C; Tier 2 is flecs-specific.")
        markdown.appendLine()

        val tiers = listOf(
            1 to tier1Benchmarks,
            2 to tier2Benchmarks
        )

        tiers.forEach { (tier, benchmarks) ->
            markdown.appendLine("## Tier $tier")
            markdown.appendLine()
            entityCounts.forEach { n ->
                markdown.appendLine("### N = ${String.format(Locale.US, "%,d", n)}")
                markdown.appendLine()
                markdown.appendLine(
                    "| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |"
                )
                markdown.appendLine("|---|---|---|---|---|---|")
                benchmarks.forEach { benchmark ->
                    markdown.appendLine(
                        "| $benchmark | " +
                            "${cell(tier, benchmark, n, "flecs-c")} | " +
                            "${cell(tier, benchmark, n, "flecs-java")} | " +
                            "${cell(tier, benchmark, n, "artemis-odb")} | " +
                            "${cell(tier, benchmark, n, "dominion-ecs")} | " +
                            "${javaRatio(tier, benchmark, n)} |"
                    )
                }
                markdown.appendLine()
            }
        }

        val csv = StringBuilder()
        csv.appendLine("# Benchmark results")
        csv.appendLine("# generated: $generated")
        csv.appendLine("# git_commit: $commit")
        csv.appendLine("# mode: ${if (benchQuick) "quick" else "full"}")
        machineInfo.forEach { (key, value) ->
            csv.appendLine("# machine_$key: $value")
        }
        artifactVersions.forEach { (library, version) ->
            csv.appendLine("# version_${library.replace(' ', '_')}: $version")
        }
        csv.appendLine("tier,benchmark,unit,n,impl,ns_per_unit,ratio_vs_flecs_c")
        tiers.forEach { (tier, benchmarks) ->
            benchmarks.forEach { benchmark ->
                entityCounts.forEach { n ->
                    val baseline = valueOf(tier, benchmark, n, "flecs-c")
                    implOrder.forEach { impl ->
                        val value = valueOf(tier, benchmark, n, impl) ?: return@forEach
                        val ratio = if (baseline != null && baseline > 0.0) {
                            formatRatio(value / baseline)
                        } else {
                            ""
                        }
                        csv.appendLine(
                            "$tier,$benchmark,${benchmarkUnits[benchmark] ?: ""},$n,$impl," +
                                "${formatNumber(value)},$ratio"
                        )
                    }
                }
            }
        }

        markdownReportFile.writeText(markdown.toString())
        csvReportFile.writeText(csv.toString())

        println("Benchmark report: ${markdownReportFile.absolutePath}")
        println("Benchmark CSV:    ${csvReportFile.absolutePath}")
    }
}

val benchmarkAll by tasks.registering {
    group = "benchmark"
    dependsOn("jmh", runCBenchmark, mergeBenchmarkResults)
}

tasks.named("jmh") {
    outputs.upToDateWhen { false }
    mustRunAfter(runCBenchmark)
}
