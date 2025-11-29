package com.github.elebras1.flecs.processor;

import com.palantir.javapoet.JavaFile;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

@SupportedAnnotationTypes("com.github.elebras1.flecs.annotation.FlecsComponent")
public class FlecsComponentProcessor extends AbstractProcessor {

    private static final Set<String> SUPPORTED_TYPES = Set.of("short", "int", "long", "float", "double", "boolean", "java.lang.String");
    private Messager messager;
    private Filer filer;
    private ComponentCodeGenerator generator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.generator = new ComponentCodeGenerator();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (roundEnv.processingOver()) {
            return false;
        }

        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {

                if (element.getKind() != ElementKind.RECORD) {
                    this.messager.printMessage(Diagnostic.Kind.ERROR, "@FlecsComponent can only be applied to records", element);
                    continue;
                }

                try {
                    processRecord((TypeElement) element);
                } catch (Exception e) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Failed to process @FlecsComponent: " + e.getMessage(), element);
                }
            }
        }

        return true;
    }

    private void processRecord(TypeElement recordElement) throws IOException {

        List<VariableElement> fields = this.extractRecordComponents(recordElement);

        for (VariableElement field : fields) {
            if (!isSupportedType(field.asType())) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Unsupported field type '" + field.asType() + "'. Supported: " + String.join(", ", SUPPORTED_TYPES), field);
                return;
            }
        }

        JavaFile javaFile = generator.generateComponentClass(recordElement, fields);
        javaFile.writeTo(filer);

        messager.printMessage(Diagnostic.Kind.NOTE, "[@FlecsComponent] Generated: " + recordElement.getQualifiedName());
    }

    private List<VariableElement> extractRecordComponents(TypeElement recordElement) {
        List<VariableElement> components = new ArrayList<>();
        for (Element e : recordElement.getEnclosedElements()) {
            if (e.getKind() == ElementKind.RECORD_COMPONENT) {
                components.add((VariableElement) e);
            }
        }
        return components;
    }

    private boolean isSupportedType(TypeMirror type) {
        return SUPPORTED_TYPES.contains(type.toString());
    }
}


