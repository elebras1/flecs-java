package com.github.elebras1.flecs;

import com.github.elebras1.flecs.util.internal.codegen.CodeBuilder;
import com.github.elebras1.flecs.util.internal.codegen.SourceFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EachBaseGenerator {

    private static final int MAX_COMPONENTS = 32;

    private static final String GENERATED_PACKAGE = "com.github.elebras1.flecs";

    private static final String ARENA_FQN = "java.lang.foreign.Arena";
    private static final String MEMORY_SEGMENT_FQN = "java.lang.foreign.MemorySegment";
    private static final String COMPONENT_FQN = "com.github.elebras1.flecs.Component";
    private static final String COMPONENT_VIEW_FQN = "com.github.elebras1.flecs.ComponentView";
    private static final String FLECS_H_FQN = "com.github.elebras1.flecs.flecs_h";
    private static final String ECS_ITER_T_FQN = "com.github.elebras1.flecs.ecs_iter_t";
    private static final String ECS_ITER_ACTION_T_FQN = "com.github.elebras1.flecs.ecs_iter_action_t";
    private static final String ECS_SYSTEM_DESC_T_FQN = "com.github.elebras1.flecs.ecs_system_desc_t";
    private static final String ECS_OBSERVER_DESC_T_FQN = "com.github.elebras1.flecs.ecs_observer_desc_t";
    private static final String WORLD_FQN = "com.github.elebras1.flecs.World";
    private static final String FLECS_SYSTEM_FQN = "com.github.elebras1.flecs.FlecsSystem";
    private static final String FLECS_OBSERVER_FQN = "com.github.elebras1.flecs.FlecsObserver";
    private static final String VALUE_LAYOUT_FQN = "java.lang.foreign.ValueLayout";

    private enum EntityMode { WITHOUT_ENTITY, WITH_ENTITY }
    private enum ViewMode { COMPONENT, COMPONENT_VIEW }

    private enum BuilderKind {
        SYSTEM(FLECS_SYSTEM_FQN, ECS_SYSTEM_DESC_T_FQN),
        OBSERVER(FLECS_OBSERVER_FQN, ECS_OBSERVER_DESC_T_FQN);

        final String returnTypeFqn;
        final String descTypeFqn;
        final String returnType;
        final String descType;

        BuilderKind(String returnTypeFqn, String descTypeFqn) {
            this.returnTypeFqn = returnTypeFqn;
            this.descTypeFqn = descTypeFqn;
            this.returnType = simpleName(returnTypeFqn);
            this.descType = simpleName(descTypeFqn);
        }
    }

    private static String simpleName(String fqn) {
        int idx = fqn.lastIndexOf('.');
        return idx >= 0 ? fqn.substring(idx + 1) : fqn;
    }

    private static String letter(int i) {
        return i < 26 ? String.valueOf((char) ('A' + i))
                : "A" + (char) ('A' + (i - 26));
    }

    private static void indent(CodeBuilder sb, int level) {
        sb.append("    ".repeat(level));
    }

    private static void appendLine(CodeBuilder sb, int level, String line) {
        indent(sb, level);
        sb.append(line).newline();
    }

    private static void appendStatement(CodeBuilder sb, int level, String statement) {
        appendLine(sb, level, statement + ";");
    }

    private String buildArgs(String prefix, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(prefix).append(letter(i));
        }
        return sb.toString();
    }

    private List<String> compTypeVars(int n) {
        List<String> vars = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            vars.add(letter(i));
        }
        return vars;
    }

    private List<String> viewTypeVars(int n) {
        List<String> vars = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            vars.add("V" + letter(i));
        }
        return vars;
    }

    private String join(List<String> items) {
        return String.join(", ", items);
    }

    private String callbackInterfaceName(int n, ViewMode vm, EntityMode em) {
        String base = vm == ViewMode.COMPONENT_VIEW ? "ComponentView" : "Component";
        String suffix = em == EntityMode.WITH_ENTITY ? "WithEntityCallback" : "Callback";
        return base + n + suffix;
    }

    public static void main(String[] args) {
        Path outputDir = args.length > 0 ? Paths.get(args[0]) : Paths.get("src/main/generated");
        EachBaseGenerator generator = new EachBaseGenerator();
        List<SourceFile> files = generator.generate();
        int count = 0;
        for (SourceFile file : files) {
            try {
                file.writeTo(outputDir);
                count++;
            } catch (IOException e) {
                throw new RuntimeException("Failed to write file: " + e.getMessage(), e);
            }
        }
        System.out.println("Generated " + count + " files in " + outputDir.toAbsolutePath());
    }

    public List<SourceFile> generate() {
        List<SourceFile> files = new ArrayList<>();
        for (int n = 1; n <= MAX_COMPONENTS; n++) {
            for (ViewMode vm : ViewMode.values()) {
                for (EntityMode em : EntityMode.values()) {
                    files.add(generateCallbackInterface(n, vm, em));
                }
            }
        }
        files.add(generateQueryBase());
        files.add(generateBuilderBase("SystemBuilderBase", BuilderKind.SYSTEM));
        files.add(generateBuilderBase("ObserverBuilderBase", BuilderKind.OBSERVER));
        return files;
    }

    private SourceFile generateCallbackInterface(int n, ViewMode vm, EntityMode em) {
        String interfaceName = callbackInterfaceName(n, vm, em);
        List<String> typeVars = vm == ViewMode.COMPONENT_VIEW ? viewTypeVars(n) : compTypeVars(n);

        CodeBuilder body = new CodeBuilder();
        body.append("@FunctionalInterface").newline();
        body.append("public interface ").append(interfaceName);
        if (!typeVars.isEmpty()) {
            if (vm == ViewMode.COMPONENT_VIEW) {
                List<String> bounds = new ArrayList<>();
                for (String v : typeVars) {
                    bounds.add(v + " extends " + simpleName(COMPONENT_VIEW_FQN));
                }
                body.append("<").append(join(bounds)).append("> ");
            } else {
                body.append("<").append(join(typeVars)).append("> ");
            }
        } else {
            body.append(" ");
        }
        body.append("{").newline();

        indent(body, 1);
        body.append("void accept(");
        List<String> params = new ArrayList<>();
        if (em == EntityMode.WITH_ENTITY) {
            params.add("long entityId");
        }
        String prefix = vm == ViewMode.COMPONENT_VIEW ? "componentView" : "component";
        for (int i = 0; i < n; i++) {
            params.add(typeVars.get(i) + " " + prefix + letter(i));
        }
        body.append(join(params)).append(");").newline();
        body.append("}").newline();

        SourceFile.Builder builder = SourceFile.builder(GENERATED_PACKAGE, interfaceName);
        if (vm == ViewMode.COMPONENT_VIEW) {
            builder.addImport(COMPONENT_VIEW_FQN);
        }
        return builder.classBody(body.toString()).build();
    }

    private SourceFile generateQueryBase() {
        CodeBuilder body = new CodeBuilder();
        body.append("public abstract class QueryBase {").newline();
        appendLine(body, 1, "protected final " + simpleName(WORLD_FQN) + " world;");
        appendLine(body, 1, "protected final " + simpleName(MEMORY_SEGMENT_FQN) + " querySeg;");
        body.newline();
        appendLine(body, 1, "protected QueryBase(" + simpleName(WORLD_FQN) + " world, " + simpleName(MEMORY_SEGMENT_FQN) + " querySeg) {");
        appendStatement(body, 2, "this.world = world");
        appendStatement(body, 2, "this.querySeg = querySeg");
        appendLine(body, 1, "}");
        body.newline();
        appendLine(body, 1, "protected abstract void checkDestroyed();");

        for (int n = 1; n <= MAX_COMPONENTS; n++) {
            for (ViewMode vm : ViewMode.values()) {
                for (EntityMode em : EntityMode.values()) {
                    body.newline();
                    appendQueryEachMethod(body, n, vm, em);
                }
            }
        }

        body.append("}").newline();

        return SourceFile.builder(GENERATED_PACKAGE, "QueryBase")
                .fileComment("Generated by CallbackGenerator.")
                .addImport(WORLD_FQN)
                .addImport(MEMORY_SEGMENT_FQN)
                .addImport(ARENA_FQN)
                .addImport(COMPONENT_FQN)
                .addImport(COMPONENT_VIEW_FQN)
                .addImport(FLECS_H_FQN)
                .addImport(ECS_ITER_T_FQN)
                .addImport(VALUE_LAYOUT_FQN)
                .classBody(body.toString())
                .build();
    }

    private void appendQueryEachMethod(CodeBuilder body, int n, ViewMode vm, EntityMode em) {
        appendEachMethodSignature(body, n, vm, em, "void", "each");
        appendStatement(body, 2, "this.checkDestroyed()");
        appendLine(body, 2, "try (" + simpleName(ARENA_FQN) + " tmpArena = " + simpleName(ARENA_FQN) + ".ofConfined()) {");
        appendStatement(body, 3, simpleName(MEMORY_SEGMENT_FQN) + " iter = " + simpleName(FLECS_H_FQN)
                + ".ecs_query_iter(tmpArena, this.world.worldSeg(), this.querySeg)");
        appendLine(body, 3, "if (iter.address() == 0) {");
        appendStatement(body, 4, "throw new IllegalStateException(\"ecs_query_iter returned a null iterator\")");
        appendLine(body, 3, "}");

        emitComponentLookups(body, 3, n, vm);

        if (vm == ViewMode.COMPONENT_VIEW) {
            appendStatement(body, 3, "this.world.viewCache().resetCursors()");
        }

        appendLine(body, 3, "while (" + simpleName(FLECS_H_FQN) + ".ecs_iter_next(iter)) {");
        if (em == EntityMode.WITH_ENTITY) {
            appendStatement(body, 4, simpleName(MEMORY_SEGMENT_FQN) + " entities = " + simpleName(ECS_ITER_T_FQN) + ".entities(iter)");
        }
        emitFieldOrBase(body, 4, n, vm, "iter");
        appendStatement(body, 4, "int count = " + simpleName(ECS_ITER_T_FQN) + ".count(iter)");
        appendLine(body, 4, "for (int i = 0; i < count; i++) {");
        if (em == EntityMode.WITH_ENTITY) {
            appendStatement(body, 5, "long entityId = entities.getAtIndex(" + simpleName(VALUE_LAYOUT_FQN) + ".JAVA_LONG, i)");
        }
        emitInstanceOrView(body, 5, n, vm);
        emitCallbackAccept(body, 5, n, vm, em);
        appendLine(body, 4, "}");
        appendLine(body, 3, "}");
        appendLine(body, 2, "}");
        appendLine(body, 1, "}");
    }

    private SourceFile generateBuilderBase(String className, BuilderKind kind) {
        CodeBuilder body = new CodeBuilder();
        body.append("public abstract class ").append(className).append(" {").newline();
        appendLine(body, 1, "protected final " + simpleName(WORLD_FQN) + " world;");
        appendLine(body, 1, "protected final " + simpleName(MEMORY_SEGMENT_FQN) + " desc;");
        body.newline();
        appendLine(body, 1, "protected " + className + "(" + simpleName(WORLD_FQN) + " world, " + simpleName(MEMORY_SEGMENT_FQN) + " desc) {");
        appendStatement(body, 2, "this.world = world");
        appendStatement(body, 2, "this.desc = desc");
        appendLine(body, 1, "}");
        body.newline();
        appendLine(body, 1, "protected abstract " + kind.returnType + " build();");

        for (int n = 1; n <= MAX_COMPONENTS; n++) {
            for (ViewMode vm : ViewMode.values()) {
                for (EntityMode em : EntityMode.values()) {
                    body.newline();
                    appendBuilderEachMethod(body, n, vm, em, kind);
                }
            }
        }

        body.append("}").newline();

        return SourceFile.builder(GENERATED_PACKAGE, className)
                .fileComment("Generated by EachGenerator.")
                .addImport(WORLD_FQN)
                .addImport(MEMORY_SEGMENT_FQN)
                .addImport(COMPONENT_FQN)
                .addImport(COMPONENT_VIEW_FQN)
                .addImport(FLECS_H_FQN)
                .addImport(ECS_ITER_T_FQN)
                .addImport(ECS_ITER_ACTION_T_FQN)
                .addImport(VALUE_LAYOUT_FQN)
                .addImport(kind.returnTypeFqn)
                .addImport(kind.descTypeFqn)
                .classBody(body.toString())
                .build();
    }

    private void appendBuilderEachMethod(CodeBuilder body, int n, ViewMode vm, EntityMode em, BuilderKind kind) {
        appendEachMethodSignature(body, n, vm, em, kind.returnType, "each");
        emitComponentLookups(body, 2, n, vm);

        appendLine(body, 2, simpleName(MEMORY_SEGMENT_FQN) + " callbackStub = " + simpleName(ECS_ITER_ACTION_T_FQN)
                + ".allocate(iterSegment -> {");
        if (vm == ViewMode.COMPONENT_VIEW) {
            appendStatement(body, 3, "this.world.viewCache().resetCursors()");
        }
        if (em == EntityMode.WITH_ENTITY) {
            appendStatement(body, 3, simpleName(MEMORY_SEGMENT_FQN) + " entities = " + simpleName(ECS_ITER_T_FQN) + ".entities(iterSegment)");
        }
        emitFieldOrBase(body, 3, n, vm, "iterSegment");
        appendStatement(body, 3, "int count = " + simpleName(ECS_ITER_T_FQN) + ".count(iterSegment)");
        appendLine(body, 3, "for (int i = 0; i < count; i++) {");
        if (em == EntityMode.WITH_ENTITY) {
            appendStatement(body, 4, "long entityId = entities.getAtIndex(" + simpleName(VALUE_LAYOUT_FQN) + ".JAVA_LONG, i)");
        }
        emitInstanceOrView(body, 4, n, vm);
        emitCallbackAccept(body, 4, n, vm, em);
        appendLine(body, 3, "}");
        appendLine(body, 2, "}, this.world.arena());");

        appendStatement(body, 2, kind.descType + ".callback(this.desc, callbackStub)");
        appendStatement(body, 2, "return build()");
        appendLine(body, 1, "}");
    }

    private void appendEachMethodSignature(CodeBuilder body, int n, ViewMode vm, EntityMode em, String returnType, String methodBase) {
        if (vm == ViewMode.COMPONENT_VIEW) {
            appendLine(body, 1, "@SuppressWarnings(\"unchecked\")");
        }

        List<String> compVars = compTypeVars(n);
        List<String> viewVars = viewTypeVars(n);
        List<String> typeDecls = new ArrayList<>(compVars);
        if (vm == ViewMode.COMPONENT_VIEW) {
            for (String v : viewVars) {
                typeDecls.add(v + " extends " + simpleName(COMPONENT_VIEW_FQN));
            }
        }

        String methodName = vm == ViewMode.COMPONENT_VIEW ? methodBase + "View" : methodBase;
        StringBuilder signature = new StringBuilder();
        signature.append("public ");
        if (!typeDecls.isEmpty()) {
            signature.append("<").append(join(typeDecls)).append("> ");
        }
        signature.append(returnType).append(" ").append(methodName).append("(");

        List<String> params = new ArrayList<>();
        for (String compVar : compVars) {
            params.add("Class<" + compVar + "> componentClass" + compVar);
        }

        String callbackName = callbackInterfaceName(n, vm, em);
        List<String> callbackArgs = vm == ViewMode.COMPONENT_VIEW ? viewVars : compVars;
        String callbackType = callbackName + "<" + join(callbackArgs) + ">";
        params.add(callbackType + " callback");

        signature.append(join(params)).append(") {");
        appendLine(body, 1, signature.toString());
    }

    private void emitComponentLookups(CodeBuilder body, int level, int n, ViewMode vm) {
        for (int i = 0; i < n; i++) {
            String comp = letter(i);
            if (vm == ViewMode.COMPONENT_VIEW) {
                appendStatement(body, level, simpleName(COMPONENT_FQN) + "<?> component" + comp
                        + " = this.world.componentRegistry().getComponent(componentClass" + comp + ")");
            } else {
                appendStatement(body, level, simpleName(COMPONENT_FQN) + "<" + comp + "> component" + comp
                        + " = this.world.componentRegistry().getComponent(componentClass" + comp + ")");
            }
        }
        if (vm == ViewMode.COMPONENT_VIEW) {
            for (int i = 0; i < n; i++) {
                String comp = letter(i);
                String view = "V" + comp;
                appendStatement(body, level, view + " componentView" + comp + " = (" + view
                        + ") this.world.viewCache().getComponentView(componentClass" + comp + ")");
            }
        }
        for (int i = 0; i < n; i++) {
            String comp = letter(i);
            appendStatement(body, level, "long size" + comp + " = component" + comp + ".size()");
        }
    }

    private void emitFieldOrBase(CodeBuilder body, int level, int n, ViewMode vm, String iterVar) {
        for (int i = 0; i < n; i++) {
            String comp = letter(i);
            if (vm == ViewMode.COMPONENT_VIEW) {
                appendStatement(body, level, "long base" + comp + " = " + simpleName(FLECS_H_FQN)
                        + ".ecs_field_w_size(" + iterVar + ", size" + comp + ", (byte) " + i + ").address()");
            } else {
                appendStatement(body, level, simpleName(MEMORY_SEGMENT_FQN) + " field" + comp + " = "
                        + simpleName(FLECS_H_FQN) + ".ecs_field_w_size(" + iterVar + ", size" + comp + ", (byte) " + i + ")");
            }
        }
    }

    private void emitInstanceOrView(CodeBuilder body, int level, int n, ViewMode vm) {
        for (int i = 0; i < n; i++) {
            String comp = letter(i);
            if (vm == ViewMode.COMPONENT_VIEW) {
                appendStatement(body, level, "componentView" + comp + ".setBaseAddress(base" + comp
                        + " + (long) i * size" + comp + ")");
            } else {
                appendStatement(body, level, comp + " componentInstance" + comp + " = component" + comp
                        + ".read(field" + comp + ", (long) i * size" + comp + ")");
            }
        }
    }

    private void emitCallbackAccept(CodeBuilder body, int level, int n, ViewMode vm, EntityMode em) {
        String args = buildArgs(vm == ViewMode.COMPONENT_VIEW ? "componentView" : "componentInstance", n);
        if (em == EntityMode.WITH_ENTITY) {
            appendStatement(body, level, "callback.accept(entityId, " + args + ")");
        } else {
            appendStatement(body, level, "callback.accept(" + args + ")");
        }
    }
}
