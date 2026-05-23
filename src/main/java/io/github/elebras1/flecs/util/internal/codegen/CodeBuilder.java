package io.github.elebras1.flecs.util.internal.codegen;

public final class CodeBuilder {
    private final StringBuilder stringBuilder;

    public CodeBuilder() {
        this.stringBuilder = new StringBuilder();
    }

    public CodeBuilder append(String content) {
        this.stringBuilder.append(content);
        return this;
    }

    public CodeBuilder append(int content) {
        this.stringBuilder.append(content);
        return this;
    }

    public CodeBuilder append(long content) {
        this.stringBuilder.append(content);
        return this;
    }

    public CodeBuilder indent4() {
        this.stringBuilder.append("    ");
        return this;
    }

    public CodeBuilder indent8() {
        this.stringBuilder.append("        ");
        return this;
    }

    public CodeBuilder indent12() {
        this.stringBuilder.append("            ");
        return this;
    }

    public CodeBuilder newline() {
        this.stringBuilder.append("\n");
        return this;
    }

    @Override
    public String toString() {
        return this.stringBuilder.toString();
    }
}