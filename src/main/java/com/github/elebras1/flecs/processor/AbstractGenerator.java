package com.github.elebras1.flecs.processor;

import com.github.elebras1.flecs.annotation.FixedArray;
import com.github.elebras1.flecs.annotation.FixedString;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static com.github.elebras1.flecs.processor.ComponentGenerator.DEFAULT_STRING_SIZE;

public abstract class AbstractGenerator implements Generator {

    protected String getPackageName(TypeElement element) {
        Element current = element.getEnclosingElement();
        while (current != null && !(current instanceof PackageElement)) {
            current = current.getEnclosingElement();
        }
        return current != null ? ((PackageElement) current).getQualifiedName().toString() : "";
    }

    protected String getGetterMethod(String type) {
        return switch (type) {
            case "byte" -> "getByte";
            case "short" -> "getShort";
            case "int" -> "getInt";
            case "long" -> "getLong";
            case "float" -> "getFloat";
            case "double" -> "getDouble";
            case "boolean" -> "getBoolean";
            case "byte[]" -> "getByteArray";
            case "short[]" -> "getShortArray";
            case "int[]" -> "getIntArray";
            case "long[]" -> "getLongArray";
            case "float[]" -> "getFloatArray";
            case "double[]" -> "getDoubleArray";
            case "boolean[]" -> "getBooleanArray";
            case "java.lang.String" -> "getFixedString";
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    protected int getStringSize(VariableElement field) {
        FixedString annotation = field.getAnnotation(FixedString.class);
        if (annotation != null) {
            int size = annotation.size();
            if ((size & (size - 1)) != 0) {
                throw new IllegalArgumentException("Field '" + field.getSimpleName() + "': @FixedString size must be a power of 2. Got: " + size);
            }
            return size;
        }
        return DEFAULT_STRING_SIZE;
    }

    protected int getArrayLength(VariableElement field) {
        FixedArray annotation = field.getAnnotation(FixedArray.class);
        if (annotation != null) {
            int length = annotation.length();
            if (length <= 0) {
                throw new IllegalArgumentException("Field '" + field.getSimpleName() + "': @FixedArray length must be greater than 0. Got: " + length);
            }
            return length;
        }
        throw new IllegalArgumentException("Field '" + field.getSimpleName() + "': Missing @FixedArray annotation for array type.");
    }
}
