package io.github.elebras1.flecs;

import io.github.elebras1.flecs.util.internal.codegen.CodeBuilder;
import io.github.elebras1.flecs.util.internal.codegen.SourceFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBaseGenerator {

    protected static final int MAX_COMPONENTS = 32;

    protected static final String GENERATED_PACKAGE = "io.github.elebras1.flecs";

    protected static final String ARENA_FQN = "java.lang.foreign.Arena";
    protected static final String MEMORY_SEGMENT_FQN = "java.lang.foreign.MemorySegment";
    protected static final String VALUE_LAYOUT_FQN = "java.lang.foreign.ValueLayout";
    protected static final String COMPONENT_FQN = "io.github.elebras1.flecs.Component";
    protected static final String COMPONENT_VIEW_FQN = "io.github.elebras1.flecs.ComponentView";
    protected static final String FLECS_H_FQN = "io.github.elebras1.flecs.flecs_h";
    protected static final String ID_FQN = "io.github.elebras1.flecs.Id";
    protected static final String WORLD_FQN = "io.github.elebras1.flecs.World";
    protected static final String ECS_RECORD_T_FQN = "io.github.elebras1.flecs.ecs_record_t";
    protected static final String ECS_TABLE_DIFF_T_FQN = "io.github.elebras1.flecs.ecs_table_diff_t";
    protected static final String ECS_TYPE_T_FQN = "io.github.elebras1.flecs.ecs_type_t";

    protected enum EntityMode { WITHOUT_ENTITY, WITH_ENTITY }
    protected enum ViewMode { COMPONENT, COMPONENT_VIEW }

    protected static String simpleName(String fqn) {
        int idx = fqn.lastIndexOf('.');
        return idx >= 0 ? fqn.substring(idx + 1) : fqn;
    }

    protected static String letter(int i) {
        return i < 26 ? String.valueOf((char) ('A' + i))
                : "A" + (char) ('A' + (i - 26));
    }

    protected static void indent(CodeBuilder sb, int level) {
        sb.append("    ".repeat(level));
    }

    protected static void appendLine(CodeBuilder sb, int level, String line) {
        indent(sb, level);
        sb.append(line).newline();
    }

    protected static void appendStatement(CodeBuilder sb, int level, String statement) {
        appendLine(sb, level, statement + ";");
    }

    protected static String buildArgs(String prefix, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(prefix).append(letter(i));
        }
        return sb.toString();
    }

    protected static List<String> compTypeVars(int n) {
        List<String> vars = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            vars.add(letter(i));
        }
        return vars;
    }

    protected static List<String> viewTypeVars(int n) {
        List<String> vars = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            vars.add("V" + letter(i));
        }
        return vars;
    }

    protected static String join(List<String> items) {
        return String.join(", ", items);
    }

    protected static String callbackInterfaceName(int n, ViewMode vm, EntityMode em) {
        String base = vm == ViewMode.COMPONENT_VIEW ? "ComponentView" : "Component";
        String suffix = em == EntityMode.WITH_ENTITY ? "WithEntityCallback" : "Callback";
        return base + n + suffix;
    }

    protected static String predicateInterfaceName(int n, ViewMode vm) {
        String base = vm == ViewMode.COMPONENT_VIEW ? "ComponentView" : "Component";
        return base + n + "Predicate";
    }

    protected int generateAndWrite(Path outputDir) {
        int count = 0;
        for (SourceFile file : this.generate()) {
            try {
                file.writeTo(outputDir);
                count++;
            } catch (IOException e) {
                throw new RuntimeException("Failed to write file: " + e.getMessage(), e);
            }
        }
        return count;
    }

    protected abstract List<SourceFile> generate();
}