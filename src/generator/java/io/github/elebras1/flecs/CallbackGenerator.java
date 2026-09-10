package io.github.elebras1.flecs;

import io.github.elebras1.flecs.internal.codegen.CodeBuilder;
import io.github.elebras1.flecs.internal.codegen.SourceFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CallbackGenerator extends AbstractBaseGenerator {

    public List<SourceFile> generate() {
        List<SourceFile> files = new ArrayList<>();
        for (int n = 1; n <= MAX_COMPONENTS; n++) {
            for (ViewMode vm : ViewMode.values()) {
                for (EntityMode em : EntityMode.values()) {
                    files.add(generateCallbackInterface(n, vm, em));
                }
                files.add(generatePredicateInterface(n, vm));
            }
        }
        return files;
    }

    private SourceFile generateCallbackInterface(int n, ViewMode vm, EntityMode em) {
        String interfaceName = callbackInterfaceName(n, vm, em);
        List<String> typeVars = vm == ViewMode.COMPONENT_VIEW ? viewTypeVars(n) : compTypeVars(n);

        CodeBuilder body = new CodeBuilder();
        body.append("@FunctionalInterface").newline();
        body.append("public interface ").append(interfaceName);
        if (!typeVars.isEmpty()) {
            appendTypeDecl(body, typeVars, vm);
        } else {
            body.append(" ");
        }
        body.append("{").newline();

        indent(body, 1);
        body.append("void accept(");
        List<String> params = new ArrayList<>();
        if (em == EntityMode.WITH_ENTITY) {
            params.add("long entityId");
        } else if (em == EntityMode.WITH_ITER) {
            params.add("Iter iter");
            params.add("int index");
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

    private SourceFile generatePredicateInterface(int n, ViewMode vm) {
        String interfaceName = predicateInterfaceName(n, vm);
        List<String> typeVars = vm == ViewMode.COMPONENT_VIEW ? viewTypeVars(n) : compTypeVars(n);

        CodeBuilder body = new CodeBuilder();
        body.append("@FunctionalInterface").newline();
        body.append("public interface ").append(interfaceName);
        if (!typeVars.isEmpty()) {
            appendTypeDecl(body, typeVars, vm);
        } else {
            body.append(" ");
        }
        body.append("{").newline();

        indent(body, 1);
        body.append("boolean test(");
        List<String> params = new ArrayList<>();
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

    private void appendTypeDecl(CodeBuilder body, List<String> typeVars, ViewMode vm) {
        if (vm == ViewMode.COMPONENT_VIEW) {
            List<String> bounds = new ArrayList<>();
            for (String v : typeVars) {
                bounds.add(v + " extends " + simpleName(COMPONENT_VIEW_FQN));
            }
            body.append("<").append(join(bounds)).append("> ");
        } else {
            body.append("<").append(join(typeVars)).append("> ");
        }
    }

    public static void main(String[] args) {
        Path outputDir = args.length > 0 ? Paths.get(args[0]) : Paths.get("src/main/generated");
        CallbackGenerator generator = new CallbackGenerator();
        int count = generator.generateAndWrite(outputDir);
        System.out.println("Generated " + count + " files in " + outputDir.toAbsolutePath());
    }
}