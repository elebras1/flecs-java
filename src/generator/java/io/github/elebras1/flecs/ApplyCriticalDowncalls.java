package io.github.elebras1.flecs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ApplyCriticalDowncalls {

    private static final List<String> CRITICAL_FUNCTIONS = List.of(
            "ecs_cpp_new",
            "ecs_exists",
            "ecs_field_is_self",
            "ecs_field_w_size",
            "ecs_get_id",
            "ecs_get_mut_id",
            "ecs_get_typeid",
            "ecs_has_id",
            "ecs_is_alive",
            "ecs_lookup",
            "ecs_lookup_child",
            "ecs_lookup_path_w_sep",
            "ecs_make_pair",
            "ecs_new",
            "ecs_new_w_parent",
            "ecs_owns_id",
            "ecs_record_find",
            "ecs_record_get_by_column",
            "ecs_ref_get_id",
            "ecs_stage_get_id",
            "ecs_table_get_column_index",
            "flecs_table_traverse_add");

    private static final List<String> POINTER_RESULT_AS_LONG = List.of(
            "ecs_get_id",
            "ecs_get_mut_id",
            "ecs_ref_get_id");

    private static final Map<String, Set<String>> CALLBACK_POINTER_PARAMS_AS_LONG = Map.of(
            "ecs_order_by_action_t", Set.of("ptr1", "ptr2"));

    private static final String DOWN_CALL = "HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC";
    private static final String CRITICAL_OPTION = ", Linker.Option.critical(false)";
    private static final Pattern ADDRESS = Pattern.compile("findOrThrow\\(\"([^\"]+)\"\\)");
    private static final Pattern HOLDER_CLASS = Pattern.compile("^    private static class (\\w+) \\{$");
    private static final String DESC_OF = "public static final FunctionDescriptor DESC = FunctionDescriptor.of(";
    private static final String C_POINTER = "flecs_h.C_POINTER";
    private static final String C_LONG = "flecs_h.C_LONG";
    private static final String C_LONG_CANONICAL =
            "public static final ValueLayout.OfLong C_LONG = (ValueLayout.OfLong) Linker.nativeLinker().canonicalLayouts().get(\"long\");";
    private static final String C_LONG_JAVA = "public static final ValueLayout.OfLong C_LONG = ValueLayout.JAVA_LONG;";

    private ApplyCriticalDowncalls() {
    }

    public static Set<String> criticalFunctions() {
        return new LinkedHashSet<>(CRITICAL_FUNCTIONS);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: ApplyCriticalDowncalls <generatedSourcesDir>");
            System.exit(2);
        }

        Path generatedDir = Path.of(args[0]);
        Set<String> critical = criticalFunctions();
        Set<String> pointerResults = new LinkedHashSet<>(POINTER_RESULT_AS_LONG);

        Set<String> handledCritical = new LinkedHashSet<>();
        Set<String> handledPointerResults = new LinkedHashSet<>();
        Set<String> handledCallbacks = new LinkedHashSet<>();
        boolean handledCLong = false;
        int changedFiles = 0;

        for (Path source : generatedSources(generatedDir)) {
            String fileName = source.getFileName().toString();
            List<String> original = Files.readAllLines(source);
            List<String> lines = new ArrayList<>(original);

            if (fileName.equals("flecs_h$shared.java")) {
                handledCLong |= applyCLong(lines);
            }

            String typeName = fileName.substring(0, fileName.length() - ".java".length());
            Set<String> callbackParams = CALLBACK_POINTER_PARAMS_AS_LONG.get(typeName);
            if (callbackParams != null) {
                applyCallbackPointerParams(lines, callbackParams);
                handledCallbacks.add(typeName);
            }

            if (fileName.startsWith("flecs_h")) {
                applyCritical(lines, critical, handledCritical);
                applyPointerResults(lines, pointerResults, handledPointerResults);
            }

            if (!lines.equals(original)) {
                Files.write(source, lines);
                changedFiles++;
            }
        }

        boolean failed = reportMissing("critical(false)", critical, handledCritical);
        failed |= reportMissing("pointer result as long", pointerResults, handledPointerResults);
        failed |= reportMissing("callback pointer params as long", CALLBACK_POINTER_PARAMS_AS_LONG.keySet(), handledCallbacks);
        if (!handledCLong) {
            System.err.println("C_LONG declaration not found in flecs_h$shared.java");
            failed = true;
        }
        if (failed) {
            System.err.println("Check that these declarations still exist in the generated bindings.");
            System.exit(1);
        }

        System.out.printf("binding rules applied: %d critical, %d pointer results, %d callbacks, %d files changed%n",
                critical.size(), pointerResults.size(), handledCallbacks.size(), changedFiles);
    }

    private static boolean reportMissing(String rule, Set<String> expected, Set<String> handled) {
        Set<String> missing = new LinkedHashSet<>(expected);
        missing.removeAll(handled);
        if (missing.isEmpty()) {
            return false;
        }
        System.err.println(rule + " could not be applied to: " + missing);
        return true;
    }

    private static boolean applyCLong(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains(C_LONG_CANONICAL)) {
                lines.set(i, line.replace(C_LONG_CANONICAL, C_LONG_JAVA));
                return true;
            }
            if (line.contains(C_LONG_JAVA)) {
                return true;
            }
        }
        return false;
    }

    private static void applyCritical(List<String> lines, Set<String> functions, Set<String> handled) {
        String currentFunction = null;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            Matcher address = ADDRESS.matcher(line);
            if (address.find()) {
                currentFunction = address.group(1);
            }

            if (currentFunction == null || !line.contains(DOWN_CALL)) {
                continue;
            }

            boolean listed = functions.contains(currentFunction);
            boolean marked = line.contains("critical(false)");

            if (listed && !marked) {
                lines.set(i, line.replace("downcallHandle(ADDR, DESC);",
                        "downcallHandle(ADDR, DESC" + CRITICAL_OPTION + ");"));
                handled.add(currentFunction);
            } else if (listed) {
                handled.add(currentFunction);
            } else if (marked) {
                lines.set(i, line.replace(CRITICAL_OPTION, ""));
            }
        }
    }

    private static void applyPointerResults(List<String> lines, Set<String> functions, Set<String> handled) {
        for (String function : functions) {
            int holder = findHolder(lines, function);
            if (holder < 0) {
                continue;
            }
            if (!lines.get(holder + 1).trim().equals(DESC_OF)) {
                throw new IllegalStateException("Unexpected descriptor for " + function);
            }
            int resultLine = holder + 2;
            lines.set(resultLine, lines.get(resultLine).replace(C_POINTER, C_LONG));

            String pointerSignature = "    public static MemorySegment " + function + "(";
            String longSignature = "    public static long " + function + "(";
            int wrapper = -1;
            for (int i = resultLine; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith(pointerSignature) || line.startsWith(longSignature)) {
                    wrapper = i;
                    break;
                }
            }
            if (wrapper < 0) {
                throw new IllegalStateException("Wrapper method not found for " + function);
            }
            lines.set(wrapper, lines.get(wrapper).replace(pointerSignature, longSignature));
            for (int i = wrapper + 1; !lines.get(i).equals("    }"); i++) {
                lines.set(i, lines.get(i).replace("return (MemorySegment)mh$.invokeExact(", "return (long)mh$.invokeExact("));
            }
            handled.add(function);
        }
    }

    private static int findHolder(List<String> lines, String function) {
        for (int i = 0; i < lines.size(); i++) {
            Matcher matcher = HOLDER_CLASS.matcher(lines.get(i));
            if (matcher.matches() && matcher.group(1).equals(function)) {
                return i;
            }
        }
        return -1;
    }

    private static void applyCallbackPointerParams(List<String> lines, Set<String> params) {
        List<String> paramNames = null;
        int descStart = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();
            if (trimmed.matches("\\w+ apply\\(.*\\);")) {
                String inner = trimmed.substring(trimmed.indexOf('(') + 1, trimmed.lastIndexOf(')'));
                paramNames = new ArrayList<>();
                for (String param : inner.split(",")) {
                    String p = param.trim();
                    paramNames.add(p.substring(p.lastIndexOf(' ') + 1));
                }
                lines.set(i, rewriteParams(line, params));
            } else if (trimmed.startsWith("public static ") && trimmed.contains(" invoke(MemorySegment funcPtr")) {
                lines.set(i, rewriteParams(line, params));
            } else if (trimmed.startsWith("private static final FunctionDescriptor $DESC = FunctionDescriptor.of(")) {
                descStart = i;
            }
        }
        if (paramNames == null || descStart < 0) {
            throw new IllegalStateException("Unexpected function pointer layout");
        }
        for (int p = 0; p < paramNames.size(); p++) {
            if (params.contains(paramNames.get(p))) {
                int descLine = descStart + 2 + p;
                lines.set(descLine, lines.get(descLine).replace(C_POINTER, C_LONG));
            }
        }
    }

    private static String rewriteParams(String line, Set<String> params) {
        String result = line;
        for (String param : params) {
            result = result.replace("MemorySegment " + param + ",", "long " + param + ",")
                    .replace("MemorySegment " + param + ")", "long " + param + ")");
        }
        return result;
    }

    private static List<Path> generatedSources(Path generatedDir) throws IOException {
        if (!Files.isDirectory(generatedDir)) {
            throw new IOException("Generated sources directory not found: " + generatedDir.toAbsolutePath());
        }
        try (Stream<Path> files = Files.walk(generatedDir)) {
            List<Path> sources = new ArrayList<>();
            files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .sorted()
                    .forEach(sources::add);
            return sources;
        }
    }
}
