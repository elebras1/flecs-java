package io.github.elebras1.flecs.util.internal.codegen;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SortedSet;
import java.util.TreeSet;

public final class SourceFile {
    private final String packageName;
    private final String simpleName;
    private final String source;

    private SourceFile(String packageName, String simpleName, String source) {
        this.packageName = packageName;
        this.simpleName = simpleName;
        this.source = source;
    }

    public void writeTo(Filer filer) throws IOException {
        String qualifiedName = this.packageName.isEmpty() ? this.simpleName : this.packageName + "." + this.simpleName;
        JavaFileObject file = filer.createSourceFile(qualifiedName);
        try (Writer writer = file.openWriter()) {
            writer.write(this.source);
        }
    }

    public void writeTo(Path outputDir) throws IOException {
        Path targetDir = this.packageName.isEmpty()
                ? outputDir
                : outputDir.resolve(this.packageName.replace('.', '/'));
        Files.createDirectories(targetDir);
        Path targetFile = targetDir.resolve(this.simpleName + ".java");
        Files.writeString(targetFile, this.source);
    }

    public static Builder builder(String packageName, String simpleName) {
        return new Builder(packageName, simpleName);
    }

    public static final class Builder {
        private final String packageName;
        private final String simpleName;
        private String fileComment = "";
        private final SortedSet<String> imports = new TreeSet<>();
        private final CodeBuilder classBody = new CodeBuilder();

        private Builder(String packageName, String simpleName) {
            this.packageName = packageName;
            this.simpleName = simpleName;
        }

        public Builder fileComment(String comment) {
            this.fileComment = comment;
            return this;
        }

        public Builder addImport(String fqn) {
            if (fqn == null || fqn.isEmpty()) return this;
            String pkg = fqn.contains(".") ? fqn.substring(0, fqn.lastIndexOf('.')) : "";
            if (!"java.lang".equals(pkg)) {
                this.imports.add(fqn);
            }
            return this;
        }

        public Builder classBody(String body) {
            this.classBody.append(body);
            return this;
        }

        public SourceFile build() {
            CodeBuilder sb = new CodeBuilder();

            if (!this.fileComment.isEmpty()) {
                sb.append("// ").append(this.fileComment).newline();
            }
            if (!this.packageName.isEmpty()) {
                sb.append("package ").append(this.packageName).append(";").newline();
            }
            if (!this.imports.isEmpty()) {
                sb.newline();
                for (String imp : this.imports) {
                    sb.append("import ").append(imp).append(";").newline();
                }
            }
            sb.newline();
            sb.append(this.classBody.toString());

            return new SourceFile(this.packageName, this.simpleName, sb.toString());
        }
    }
}