package com.github.elebras1.flecs.processor;

import com.github.elebras1.flecs.util.internal.codegen.SourceFile;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

public interface Generator {

    SourceFile generate(TypeElement recordElement, List<VariableElement> fields);
}