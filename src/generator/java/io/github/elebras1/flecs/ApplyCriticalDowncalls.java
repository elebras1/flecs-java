package io.github.elebras1.flecs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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

    private static final String DOWN_CALL = "HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC";
    private static final String CRITICAL_OPTION = ", Linker.Option.critical(false)";
    private static final Pattern ADDRESS = Pattern.compile("findOrThrow\\(\"([^\"]+)\"\\)");

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
        Set<String> functions = criticalFunctions();

        int applied = 0;
        int removed = 0;
        Set<String> handled = new LinkedHashSet<>();

        for (Path source : generatedSources(generatedDir)) {
            List<String> lines = Files.readAllLines(source);
            String currentFunction = null;
            boolean changed = false;

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
                    applied++;
                    changed = true;
                } else if (listed) {
                    handled.add(currentFunction);
                } else if (marked) {
                    lines.set(i, line.replace(CRITICAL_OPTION, ""));
                    removed++;
                    changed = true;
                }
            }

            if (changed) {
                Files.write(source, lines);
            }
        }

        Set<String> missing = new LinkedHashSet<>(functions);
        missing.removeAll(handled);
        if (!missing.isEmpty()) {
            System.err.println("critical(false) could not be applied to: " + missing);
            System.err.println("Check that these functions still exist in the generated bindings.");
            System.exit(1);
        }

        System.out.printf("critical(false): %d applied, %d removed, %d listed%n",
                applied, removed, functions.size());
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
