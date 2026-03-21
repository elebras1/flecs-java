package com.github.elebras1.flecs;

import com.palantir.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EachBaseGenerator {

    private static final int MAX_COMPONENTS = 32;

    private static final ClassName ARENA = ClassName.get("java.lang.foreign", "Arena");
    private static final ClassName MEMORY_SEGMENT = ClassName.get("java.lang.foreign", "MemorySegment");
    private static final ClassName COMPONENT = ClassName.get("com.github.elebras1.flecs", "Component");
    private static final ClassName COMPONENT_VIEW = ClassName.get("com.github.elebras1.flecs", "ComponentView");
    private static final ClassName FLECS_H = ClassName.get("com.github.elebras1.flecs", "flecs_h");
    private static final ClassName ECS_ITER_T = ClassName.get("com.github.elebras1.flecs", "ecs_iter_t");
    private static final ClassName ECS_ITER_ACTION_T = ClassName.get("com.github.elebras1.flecs", "ecs_iter_action_t");
    private static final ClassName ECS_SYSTEM_DESC_T = ClassName.get("com.github.elebras1.flecs", "ecs_system_desc_t");
    private static final ClassName ECS_OBSERVER_DESC_T = ClassName.get("com.github.elebras1.flecs", "ecs_observer_desc_t");
    private static final ClassName WORLD = ClassName.get("com.github.elebras1.flecs", "World");
    private static final ClassName FLECS_SYSTEM = ClassName.get("com.github.elebras1.flecs", "FlecsSystem");
    private static final ClassName FLECS_OBSERVER = ClassName.get("com.github.elebras1.flecs", "FlecsObserver");
    private static final ClassName VALUE_LAYOUT = ClassName.get("java.lang.foreign", "ValueLayout");

    private static final String GENERATED_PACKAGE = "com.github.elebras1.flecs";

    private enum EntityMode { WITHOUT_ENTITY, WITH_ENTITY }
    private enum ViewMode { COMPONENT, COMPONENT_VIEW }

    private enum BuilderKind {
        SYSTEM(FLECS_SYSTEM, ECS_SYSTEM_DESC_T),
        OBSERVER(FLECS_OBSERVER, ECS_OBSERVER_DESC_T);

        final ClassName returnType;
        final ClassName descType;

        BuilderKind(ClassName returnType, ClassName descType) {
            this.returnType = returnType;
            this.descType = descType;
        }
    }

    @FunctionalInterface
    private interface CodeEmitter {
        void addStatement(String format, Object... args);

        static CodeEmitter of(MethodSpec.Builder mb) {
            return mb::addStatement;
        }

        static CodeEmitter of(CodeBlock.Builder cb) {
            return cb::addStatement;
        }
    }

    private static String letter(int i) {
        return i < 26 ? String.valueOf((char) ('A' + i))
                : "A" + (char) ('A' + (i - 26));
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

    private List<TypeVariableName> compTypeVars(int n) {
        List<TypeVariableName> vars = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            vars.add(TypeVariableName.get(letter(i)));
        }
        return vars;
    }

    private List<TypeVariableName> viewTypeVars(int n) {
        List<TypeVariableName> vars = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            vars.add(TypeVariableName.get("V" + letter(i), COMPONENT_VIEW));
        }
        return vars;
    }

    private String callbackInterfaceName(int n, ViewMode vm, EntityMode em) {
        String base = vm == ViewMode.COMPONENT_VIEW ? "ComponentView" : "Component";
        String suffix = em == EntityMode.WITH_ENTITY ? "WithEntityCallback" : "Callback";
        return base + n + suffix;
    }

    public static void main(String[] args) {
        Path outputDir = args.length > 0 ? Paths.get(args[0]) : Paths.get("src/main/generated");
        EachBaseGenerator generator = new EachBaseGenerator();
        List<JavaFile> files = generator.generate();
        int count = 0;
        for (JavaFile file : files) {
            try {
                file.writeTo(outputDir);
                count++;
            } catch (IOException e) {
                throw new RuntimeException("Failed to write file " + file.typeSpec().name() + ": " + e.getMessage(), e);
            }
        }
        System.out.println("Generated " + count + " files in " + outputDir.toAbsolutePath());
    }

    public List<JavaFile> generate() {
        List<JavaFile> files = new ArrayList<>();
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

    private JavaFile generateCallbackInterface(int n, ViewMode vm, EntityMode em) {
        List<TypeVariableName> typeVars = vm == ViewMode.COMPONENT_VIEW ? viewTypeVars(n) : compTypeVars(n);

        MethodSpec.Builder accept = MethodSpec.methodBuilder("accept")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(void.class);

        if (em == EntityMode.WITH_ENTITY) {
            accept.addParameter(long.class, "entityId");
        }

        String prefix = vm == ViewMode.COMPONENT_VIEW ? "componentView" : "component";
        for (int i = 0; i < n; i++) {
            accept.addParameter(typeVars.get(i), prefix + letter(i));
        }

        TypeSpec iface = TypeSpec.interfaceBuilder(callbackInterfaceName(n, vm, em))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(FunctionalInterface.class)
                .addTypeVariables(typeVars)
                .addMethod(accept.build())
                .build();

        return JavaFile.builder(GENERATED_PACKAGE, iface).build();
    }

    private JavaFile generateQueryBase() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("QueryBase")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addField(FieldSpec.builder(WORLD, "world", Modifier.PROTECTED, Modifier.FINAL).build())
                .addField(FieldSpec.builder(MEMORY_SEGMENT, "nativeQuery", Modifier.PROTECTED, Modifier.FINAL).build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PROTECTED)
                        .addParameter(WORLD, "world")
                        .addParameter(MEMORY_SEGMENT, "nativeQuery")
                        .addStatement("this.world = world")
                        .addStatement("this.nativeQuery = nativeQuery")
                        .build())
                .addMethod(MethodSpec.methodBuilder("checkClosed")
                        .addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT)
                        .returns(void.class).build());

        for (int n = 1; n <= MAX_COMPONENTS; n++) {
            for (ViewMode vm : ViewMode.values()) {
                for (EntityMode em : EntityMode.values()) {
                    classBuilder.addMethod(buildQueryEachMethod(n, vm, em));
                }
            }
        }

        return JavaFile.builder(GENERATED_PACKAGE, classBuilder.build())
                .addFileComment("Generated by CallbackGenerator.")
                .indent("    ").build();
    }

    private MethodSpec buildQueryEachMethod(int n, ViewMode vm, EntityMode em) {
        MethodSpec.Builder mb = startEachMethod(n, vm, em, void.class);

        mb.addStatement("this.checkClosed()");
        mb.beginControlFlow("try ($T tmpArena = $T.ofConfined())", ARENA, ARENA);
        mb.addStatement("$T iter = $T.ecs_query_iter(tmpArena, this.world.nativeHandle(), this.nativeQuery)",
                MEMORY_SEGMENT, FLECS_H);
        mb.beginControlFlow("if (iter == null || iter.address() == 0)");
        mb.addStatement("throw new IllegalStateException(\"ecs_query_iter returned a null iterator\")");
        mb.endControlFlow();

        emitComponentLookups(CodeEmitter.of(mb), n, vm);

        if (vm == ViewMode.COMPONENT_VIEW) {
            mb.addStatement("this.world.viewCache().resetCursors()");
        }

        mb.beginControlFlow("while ($T.ecs_iter_next(iter))", FLECS_H);
        if (em == EntityMode.WITH_ENTITY) {
            mb.addStatement("$T entities = $T.entities(iter)", MEMORY_SEGMENT, ECS_ITER_T);
        }
        emitFieldOrBase(CodeEmitter.of(mb), n, vm, "iter");
        mb.addStatement("int count = $T.count(iter)", ECS_ITER_T);
        mb.beginControlFlow("for (int i = 0; i < count; i++)");
        if (em == EntityMode.WITH_ENTITY) {
            mb.addStatement("long entityId = entities.getAtIndex($T.JAVA_LONG, i)", VALUE_LAYOUT);
        }
        emitInstanceOrView(CodeEmitter.of(mb), n, vm);
        emitCallbackAccept(CodeEmitter.of(mb), n, vm, em);
        mb.endControlFlow();
        mb.endControlFlow();
        mb.endControlFlow();

        return mb.build();
    }

    private JavaFile generateBuilderBase(String className, BuilderKind kind) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addField(FieldSpec.builder(WORLD, "world", Modifier.PROTECTED, Modifier.FINAL).build())
                .addField(FieldSpec.builder(MEMORY_SEGMENT, "desc", Modifier.PROTECTED, Modifier.FINAL).build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PROTECTED)
                        .addParameter(WORLD, "world")
                        .addParameter(MEMORY_SEGMENT, "desc")
                        .addStatement("this.world = world")
                        .addStatement("this.desc = desc")
                        .build())
                .addMethod(MethodSpec.methodBuilder("build")
                        .addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT)
                        .returns(kind.returnType).build());

        for (int n = 1; n <= MAX_COMPONENTS; n++) {
            for (ViewMode vm : ViewMode.values()) {
                for (EntityMode em : EntityMode.values()) {
                    classBuilder.addMethod(buildBuilderEachMethod(n, vm, em, kind));
                }
            }
        }

        return JavaFile.builder(GENERATED_PACKAGE, classBuilder.build())
                .addFileComment("Generated by EachGenerator.")
                .indent("    ").build();
    }

    private MethodSpec buildBuilderEachMethod(int n, ViewMode vm, EntityMode em, BuilderKind kind) {
        MethodSpec.Builder mb = startEachMethod(n, vm, em, kind.returnType);

        emitComponentLookups(CodeEmitter.of(mb), n, vm);

        CodeBlock.Builder lambda = CodeBlock.builder();
        if (vm == ViewMode.COMPONENT_VIEW) {
            lambda.addStatement("this.world.viewCache().resetCursors()");
        }
        if (em == EntityMode.WITH_ENTITY) {
            lambda.addStatement("$T entities = $T.entities(iterSegment)", MEMORY_SEGMENT, ECS_ITER_T);
        }
        emitFieldOrBase(CodeEmitter.of(lambda), n, vm, "iterSegment");
        lambda.addStatement("int count = $T.count(iterSegment)", ECS_ITER_T);
        lambda.beginControlFlow("for (int i = 0; i < count; i++)");
        if (em == EntityMode.WITH_ENTITY) {
            lambda.addStatement("long entityId = entities.getAtIndex($T.JAVA_LONG, i)", VALUE_LAYOUT);
        }
        emitInstanceOrView(CodeEmitter.of(lambda), n, vm);
        emitCallbackAccept(CodeEmitter.of(lambda), n, vm, em);
        lambda.endControlFlow();

        mb.addCode("$T callbackStub = $T.allocate(iterSegment -> {\n", MEMORY_SEGMENT, ECS_ITER_ACTION_T);
        mb.addCode(lambda.build());
        mb.addCode("}, this.world.arena());\n");
        mb.addStatement("$T.callback(this.desc, callbackStub)", kind.descType);
        mb.addStatement("return build()");

        return mb.build();
    }

    private MethodSpec.Builder startEachMethod(int n, ViewMode vm, EntityMode em, Object returnType) {
        List<TypeVariableName> compVars = compTypeVars(n);
        List<TypeVariableName> viewVars = viewTypeVars(n);
        List<TypeVariableName> allVars = new ArrayList<>(compVars);
        if (vm == ViewMode.COMPONENT_VIEW) {
            allVars.addAll(viewVars);
        }

        List<TypeVariableName> callbackTypeVars = vm == ViewMode.COMPONENT_VIEW ? viewVars : compVars;
        ClassName callbackRaw = ClassName.get(GENERATED_PACKAGE, callbackInterfaceName(n, vm, em));
        TypeName callbackType = ParameterizedTypeName.get(callbackRaw, callbackTypeVars.toArray(new TypeName[0]));

        TypeName ret = returnType instanceof TypeName ? (TypeName) returnType
                : returnType instanceof Class<?> ? TypeName.get((Class<?>) returnType)
                : ClassName.bestGuess(returnType.toString());

        MethodSpec.Builder mb = MethodSpec.methodBuilder(vm == ViewMode.COMPONENT_VIEW ? "eachView" : "each")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariables(allVars)
                .returns(ret);

        if (vm == ViewMode.COMPONENT_VIEW) {
            mb.addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                    .addMember("value", "$S", "unchecked").build());
        }

        for (TypeVariableName tv : compVars) {
            mb.addParameter(ParameterizedTypeName.get(ClassName.get("", "Class"), tv), "componentClass" + tv.name());
        }

        mb.addParameter(callbackType, "callback");
        return mb;
    }

    private void emitComponentLookups(CodeEmitter out, int n, ViewMode vm) {
        for (int i = 0; i < n; i++) {
            TypeName compParam = vm == ViewMode.COMPONENT_VIEW
                    ? ParameterizedTypeName.get(COMPONENT, WildcardTypeName.subtypeOf(Object.class))
                    : ParameterizedTypeName.get(COMPONENT, TypeVariableName.get(letter(i)));
            out.addStatement("$T component$L = this.world.componentRegistry().getComponent(componentClass$L)",
                    compParam, letter(i), letter(i));
        }
        if (vm == ViewMode.COMPONENT_VIEW) {
            for (int i = 0; i < n; i++) {
                TypeVariableName vv = TypeVariableName.get("V" + letter(i));
                out.addStatement("$T componentView$L = ($T) this.world.viewCache().getComponentView(componentClass$L)",
                        vv, letter(i), vv, letter(i));
            }
        }
        for (int i = 0; i < n; i++) {
            out.addStatement("long size$L = component$L.size()", letter(i), letter(i));
        }
    }

    private void emitFieldOrBase(CodeEmitter out, int n, ViewMode vm, String iterVar) {
        for (int i = 0; i < n; i++) {
            if (vm == ViewMode.COMPONENT_VIEW) {
                out.addStatement("long base$L = $T.ecs_field_w_size($L, size$L, (byte) $L).address()",
                        letter(i), FLECS_H, iterVar, letter(i), i);
            } else {
                out.addStatement("$T field$L = $T.ecs_field_w_size($L, size$L, (byte) $L)",
                        MEMORY_SEGMENT, letter(i), FLECS_H, iterVar, letter(i), i);
            }
        }
    }

    private void emitInstanceOrView(CodeEmitter out, int n, ViewMode vm) {
        for (int i = 0; i < n; i++) {
            if (vm == ViewMode.COMPONENT_VIEW) {
                out.addStatement("componentView$L.setBaseAddress(base$L + (long) i * size$L)",
                        letter(i), letter(i), letter(i));
            } else {
                out.addStatement("$T componentInstance$L = component$L.read(field$L, (long) i * size$L)",
                        TypeVariableName.get(letter(i)), letter(i), letter(i), letter(i), letter(i));
            }
        }
    }

    private void emitCallbackAccept(CodeEmitter out, int n, ViewMode vm, EntityMode em) {
        String args = buildArgs(vm == ViewMode.COMPONENT_VIEW ? "componentView" : "componentInstance", n);
        if (em == EntityMode.WITH_ENTITY) {
            out.addStatement("callback.accept(entityId, $L)", args);
        } else {
            out.addStatement("callback.accept($L)", args);
        }
    }
}
