package io.github.elebras1.flecs.util.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class FlecsLoader {

    private static volatile boolean LOADED = false;

    private FlecsLoader() {}

    public static synchronized void load() {
        if (LOADED) {
            return;
        }

        String explicitPath = System.getProperty("flecs.native.path");
        if (explicitPath != null) {
            System.load(explicitPath);
            LOADED = true;
            return;
        }

        String platformId = resolvePlatformId();
        String libName = resolveLibName();
        String resourcePath = "/natives/" + platformId + "/" + libName;

        if (loadFromResource(resourcePath)) {
            LOADED = true;
            return;
        }

        try {
            System.loadLibrary("flecs");
            LOADED = true;
        } catch (UnsatisfiedLinkError e) {
            throw new UnsatisfiedLinkError(
                    "Failed to load the native Flecs library.\n" +
                            "Tried:\n" +
                            "  1. System property flecs.native.path  → not set\n" +
                            "  2. Classpath resource " + resourcePath + "  → not found\n" +
                            "     Make sure one of the following is on your runtime classpath:\n" +
                            "       io.github.elebras1:flecs-java-natives-" + platformId + "\n" +
                            "       io.github.elebras1:flecs-java-natives-" + platformId + "-debug\n" +
                            "  3. System.loadLibrary(\"flecs\")  → " + e.getMessage()
            );
        }
    }

    static String resolvePlatformId() {
        return resolveOsName() + "-" + resolveArchName();
    }

    private static String resolveOsName() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            return "windows";
        }
        if (os.contains("mac")) {
            return "macos";
        }
        if (os.contains("linux") || os.contains("nux")) {
            return "linux";
        }
        throw new UnsupportedOperationException("Unsupported OS: " + os);
    }

    private static String resolveArchName() {
        String arch = System.getProperty("os.arch", "").toLowerCase();
        if (arch.equals("amd64") || arch.equals("x86_64")) {
            return "x64";
        }
        if (arch.equals("aarch64") || arch.equals("arm64"))  {
            return "aarch64";
        }
        throw new UnsupportedOperationException("Unsupported architecture: " + arch);
    }

    private static String resolveLibName() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            return "flecs.dll";
        }
        if (os.contains("mac")) {
            return "libflecs.dylib";
        }
        if (os.contains("linux") || os.contains("nux")) {
            return "libflecs.so";
        }
        throw new UnsupportedOperationException("Unsupported OS: " + os);
    }

    private static boolean loadFromResource(String resourcePath) {
        try (InputStream in = FlecsLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                return false;
            }

            String suffix = resourcePath.substring(resourcePath.lastIndexOf('.'));
            Path tmp = Files.createTempFile("flecs-native-", suffix);
            tmp.toFile().deleteOnExit();

            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            System.load(tmp.toAbsolutePath().toString());
            return true;

        } catch (IOException e) {
            return false;
        }
    }
}