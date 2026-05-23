package io.github.elebras1.flecs.processor;

import io.github.elebras1.flecs.util.internal.codegen.CodeBuilder;
import io.github.elebras1.flecs.util.internal.codegen.SourceFile;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.List;

public class ComponentMapGenerator {

    private static final String MAP_PACKAGE = "io.github.elebras1.flecs";
    private static final String MAP_COMPONENT_CLASS_NAME = "ComponentMap";

    public SourceFile generateComponentMap(List<TypeElement> components) {
        CodeBuilder body = new CodeBuilder();

        body.append("@SuppressWarnings(\"unchecked\")").newline();
        body.append("public final class ComponentMap {").newline();

        body.indent4().append("private static final int COMPONENT_COUNT = ").append(components.size()).append(";").newline();

        body.newline();
        body.indent4().append("private static final Component<?>[] COMPONENTS;").newline();

        body.newline();
        body.indent4().append("private static final Supplier<ComponentRowView>[] ROW_VIEWS;").newline();

        body.newline();
        body.indent4().append("private static final Supplier<ComponentView>[] VIEWS;").newline();

        body.newline();
        body.indent4().append("private static final ClassValue<Integer> COMPONENT_INDEX = new ClassValue<Integer>() {").newline();
        body.indent8().append("@Override").newline();
        body.indent8().append("protected Integer computeValue(Class<?> clazz) {").newline();
        for (int i = 0; i < components.size(); i++) {
            TypeElement component = components.get(i);
            String packageName = this.getPackageName(component);
            String recordName = component.getSimpleName().toString();
            String fqn = packageName.isEmpty() ? recordName : packageName + "." + recordName;
            body.indent12().append("if (clazz == ").append(fqn).append(".class) return ").append(i).append(";").newline();
        }
        body.indent12().append("return -1;").newline();
        body.indent8().append("}").newline();
        body.indent4().append("};").newline();

        body.newline();
        body.indent4().append("static {").newline();

        body.indent8().append("COMPONENTS = new Component<?>[] {").newline();
        for (TypeElement component : components) {
            String packageName = this.getPackageName(component);
            String recordName = component.getSimpleName().toString();
            String fqn = packageName.isEmpty() ? recordName + "Component" : packageName + "." + recordName + "Component";
            body.indent12().append(fqn).append(".getInstance(),").newline();
        }
        body.indent8().append("};").newline();
        body.newline();

        body.indent8().append("VIEWS = new Supplier[] {").newline();
        for (TypeElement component : components) {
            String packageName = this.getPackageName(component);
            String recordName = component.getSimpleName().toString();
            String fqn = packageName.isEmpty() ? recordName + "View" : packageName + "." + recordName + "View";
            body.indent12().append(fqn).append("::new,").newline();
        }
        body.indent8().append("};").newline();

        body.indent8().append("ROW_VIEWS = new Supplier[] {").newline();
        for (TypeElement component : components) {
            String packageName = this.getPackageName(component);
            String recordName = component.getSimpleName().toString();
            String fqn = packageName.isEmpty() ? recordName + "RowView" : packageName + "." + recordName + "RowView";
            body.indent12().append(fqn).append("::new,").newline();
        }
        body.indent8().append("};").newline();

        body.indent4().append("}").newline();

        body.newline();
        body.indent4().append("private ComponentMap() {").newline();
        body.indent4().append("}").newline();

        body.newline();
        body.indent4().append("public static int getIndex(Class<?> componentClass) {").newline();
        body.indent8().append("return COMPONENT_INDEX.get(componentClass);").newline();
        body.indent4().append("}").newline();

        body.newline();
        body.indent4().append("public static int size() {").newline();
        body.indent8().append("return COMPONENT_COUNT;").newline();
        body.indent4().append("}").newline();

        body.newline();
        body.indent4().append("public static <T> Component<T> getInstance(Class<T> componentClass) {").newline();
        body.indent8().append("int index = COMPONENT_INDEX.get(componentClass);").newline();
        body.indent8().append("return index >= 0 ? (Component<T>) COMPONENTS[index] : null;").newline();
        body.indent4().append("}").newline();

        body.newline();
        body.indent4().append("public static <T> ComponentView getView(Class<T> componentClass) {").newline();
        body.indent8().append("int index = COMPONENT_INDEX.get(componentClass);").newline();
        body.indent8().append("Supplier<ComponentView> supplier = index >= 0 ? VIEWS[index] : null;").newline();
        body.indent8().append("return supplier != null ? supplier.get() : null;").newline();
        body.indent4().append("}").newline();

        body.newline();
        body.indent4().append("public static <T> ComponentRowView getRowView(Class<T> componentClass) {").newline();
        body.indent8().append("int index = COMPONENT_INDEX.get(componentClass);").newline();
        body.indent8().append("Supplier<ComponentRowView> supplier = index >= 0 ? ROW_VIEWS[index] : null;").newline();
        body.indent8().append("return supplier != null ? supplier.get() : null;").newline();
        body.indent4().append("}").newline();

        body.append("}").newline();

        return SourceFile.builder(MAP_PACKAGE, MAP_COMPONENT_CLASS_NAME)
                .fileComment("Generated by ComponentMapGenerator.")
                .addImport("io.github.elebras1.flecs.Component")
                .addImport("io.github.elebras1.flecs.ComponentRowView")
                .addImport("io.github.elebras1.flecs.ComponentView")
                .addImport("java.util.function.Supplier")
                .classBody(body.toString())
                .build();
    }

    private String getPackageName(TypeElement element) {
        Element current = element.getEnclosingElement();
        while (current != null && !(current instanceof PackageElement)) {
            current = current.getEnclosingElement();
        }
        return current != null ? ((PackageElement) current).getQualifiedName().toString() : "";
    }
}