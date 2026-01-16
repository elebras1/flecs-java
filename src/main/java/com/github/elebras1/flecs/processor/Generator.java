package com.github.elebras1.flecs.processor;

import com.palantir.javapoet.JavaFile;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

public interface Generator {

    JavaFile generate(TypeElement recordElement, List<VariableElement> fields);
}
