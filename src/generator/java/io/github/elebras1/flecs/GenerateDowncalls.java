package io.github.elebras1.flecs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateDowncalls {

    private static final int CHUNK_SIZE = 128;
    private static final String PACKAGE = "io.github.elebras1.flecs";
    private static final Pattern NESTED = Pattern.compile("^    private static class ([A-Za-z_$][A-Za-z0-9_$]*) \\{$");
    private static final Pattern VAR_HANDLE = Pattern.compile("^(\\s*)var mh\\$ = ([A-Za-z_$][A-Za-z0-9_$]*)\\.HANDLE;$");
    private static final Pattern OPTIONS = Pattern.compile("^ADDR,\\s*DESC(?:,\\s*(.+))?$", Pattern.DOTALL);
    private static final String HANDLE_PREFIX = "public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(";

    private GenerateDowncalls() {
    }

    public static void main(String[] args) throws IOException {
        Path generatedDir = args.length > 0 ? Paths.get(args[0]) : Paths.get("src/main/generated");
        Path resourcesDir = args.length > 1 ? Paths.get(args[1]) : Paths.get("src/main/resources");
        Path packageDir = generatedDir.resolve(PACKAGE.replace('.', '/'));

        List<Path> chunkFiles = new ArrayList<>();
        for (int i = 1; ; i++) {
            Path candidate = packageDir.resolve("flecs_h_" + i + ".java");
            if (!Files.exists(candidate)) {
                break;
            }
            chunkFiles.add(candidate);
        }
        if (chunkFiles.isEmpty()) {
            throw new IllegalStateException("No flecs_h_<n>.java files found in " + packageDir);
        }

        Map<String, String> descriptors = new LinkedHashMap<>();
        Map<String, String> optionsByFunction = new LinkedHashMap<>();
        for (Path chunk : chunkFiles) {
            collectFunctions(Files.readAllLines(chunk, StandardCharsets.UTF_8), descriptors, optionsByFunction);
        }

        Map<String, String> holderByFunction = new LinkedHashMap<>();
        int index = 0;
        for (String name : descriptors.keySet()) {
            holderByFunction.put(name, "FlecsDowncalls" + (index / CHUNK_SIZE + 1));
            index++;
        }

        for (Path chunk : chunkFiles) {
            List<String> rewritten = rewriteWrappers(Files.readAllLines(chunk, StandardCharsets.UTF_8), holderByFunction);
            Files.write(chunk, rewritten, StandardCharsets.UTF_8);
        }

        writeHolders(packageDir, descriptors, optionsByFunction, holderByFunction);
        int holders = (index + CHUNK_SIZE - 1) / CHUNK_SIZE;
        writeNativeImageProperties(resourcesDir, holders);
        System.out.println("Generated " + holders + " downcall holders for " + descriptors.size() + " functions");
    }

    private static void writeNativeImageProperties(Path resourcesDir, int holders) throws IOException {
        StringBuilder args = new StringBuilder();
        for (int i = 1; i <= holders; i++) {
            if (i > 1) {
                args.append(',');
            }
            args.append(PACKAGE).append(".FlecsDowncalls").append(i);
        }
        Path directory = resourcesDir.resolve("META-INF/native-image/io.github.elebras1/flecs-java");
        Files.createDirectories(directory);
        String properties = "Args = --initialize-at-build-time=" + args + " --enable-native-access=ALL-UNNAMED\n";
        Files.writeString(directory.resolve("native-image.properties"), properties, StandardCharsets.UTF_8);
    }

    private static void collectFunctions(List<String> lines, Map<String, String> descriptors, Map<String, String> optionsByFunction) {
        for (int i = 0; i < lines.size(); i++) {
            Matcher nested = NESTED.matcher(lines.get(i));
            if (!nested.matches()) {
                continue;
            }
            String name = nested.group(1);
            String descriptor = readDescriptor(lines, i);
            String options = readOptions(lines, i);
            if (descriptor == null || options == null) {
                continue;
            }
            if (descriptors.putIfAbsent(name, descriptor) != null) {
                throw new IllegalStateException("Duplicate downcall function: " + name);
            }
            optionsByFunction.put(name, options);
        }
    }

    private static String readDescriptor(List<String> lines, int classLine) {
        for (int i = classLine + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String marker = "public static final FunctionDescriptor DESC = ";
            int at = line.indexOf(marker);
            if (at < 0) {
                if (line.equals("    }")) {
                    return null;
                }
                continue;
            }
            StringBuilder expression = new StringBuilder();
            String rest = line.substring(at + marker.length());
            int depth = 0;
            while (true) {
                for (int c = 0; c < rest.length(); c++) {
                    char ch = rest.charAt(c);
                    if (ch == ';' && depth == 0) {
                        return expression.toString().trim();
                    }
                    if (ch == '(') {
                        depth++;
                    } else if (ch == ')') {
                        depth--;
                    }
                    expression.append(ch);
                }
                expression.append('\n');
                i++;
                if (i >= lines.size()) {
                    return null;
                }
                rest = lines.get(i);
            }
        }
        return null;
    }

    private static String readOptions(List<String> lines, int classLine) {
        for (int i = classLine + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            int at = line.indexOf(HANDLE_PREFIX);
            if (at >= 0) {
                String inner = line.substring(at + HANDLE_PREFIX.length(), line.lastIndexOf(");"));
                Matcher matcher = OPTIONS.matcher(inner);
                if (!matcher.matches()) {
                    throw new IllegalStateException("Unexpected downcall form: " + line.trim());
                }
                return matcher.group(1) == null ? "" : ", " + matcher.group(1);
            }
            if (line.equals("    }")) {
                return null;
            }
        }
        return null;
    }

    private static List<String> rewriteWrappers(List<String> lines, Map<String, String> holderByFunction) {
        List<String> result = new ArrayList<>(lines.size());
        String current = null;
        for (String line : lines) {
            Matcher var = VAR_HANDLE.matcher(line);
            if (var.matches()) {
                String name = var.group(2);
                String holder = holderByFunction.get(name);
                if (holder == null) {
                    result.add(line);
                } else {
                    current = name;
                    result.add(var.group(1) + "var mh$ = " + holder + "." + name + ";");
                }
                continue;
            }
            int invoke = line.indexOf("mh$.invokeExact(");
            if (current != null && invoke >= 0) {
                String head = line.substring(0, invoke);
                String tail = line.substring(invoke + "mh$.invokeExact(".length());
                String separator = tail.stripLeading().startsWith(")") ? "" : ", ";
                result.add(head + "mh$.invokeExact(" + current + ".ADDR" + separator + tail);
                continue;
            }
            result.add(line);
        }
        return result;
    }

    private static void writeHolders(Path packageDir, Map<String, String> descriptors, Map<String, String> optionsByFunction, Map<String, String> holderByFunction) throws IOException {
        Map<String, StringBuilder> bodies = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : descriptors.entrySet()) {
            String name = entry.getKey();
            String holder = holderByFunction.get(name);
            bodies.computeIfAbsent(holder, key -> new StringBuilder())
                    .append("    static final MethodHandle ").append(name)
                    .append(" = Linker.nativeLinker().downcallHandle(")
                    .append(entry.getValue()).append(optionsByFunction.get(name)).append(");\n\n");
        }
        for (Map.Entry<String, StringBuilder> entry : bodies.entrySet()) {
            StringBuilder file = new StringBuilder();
            file.append("// Generated by flecs-java\n\n");
            file.append("package ").append(PACKAGE).append(";\n\n");
            file.append("import java.lang.invoke.*;\n");
            file.append("import java.lang.foreign.*;\n");
            file.append("import java.nio.ByteOrder;\n");
            file.append("import java.util.*;\n");
            file.append("import java.util.function.*;\n");
            file.append("import java.util.stream.*;\n\n");
            file.append("import static java.lang.foreign.ValueLayout.*;\n");
            file.append("import static java.lang.foreign.MemoryLayout.PathElement.*;\n\n");
            file.append("final class ").append(entry.getKey()).append(" {\n\n");
            file.append("    private ").append(entry.getKey()).append("() {\n    }\n\n");
            file.append(entry.getValue());
            file.append("}\n");
            Files.writeString(packageDir.resolve(entry.getKey() + ".java"), file.toString(), StandardCharsets.UTF_8);
        }
    }

}
