package com.github.elebras1.flecs.util.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class FlecsLoader {

    private static boolean loaded = false;

    private FlecsLoader() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }

        try {
            String libPath = getNativeLibraryPath();

            if (loadFromResources(libPath)) {
                loaded = true;
                return;
            }

            System.loadLibrary("flecs");
            loaded = true;

        } catch (Exception e) {
            throw new UnsatisfiedLinkError("Unable to load the native Flecs library: " + e.getMessage());
        }
    }

    private static String getArchitecture() {
        String osArch = System.getProperty("os.arch").toLowerCase();

        if (osArch.equals("amd64") || osArch.equals("x86_64")) {
            return "x64";
        } else if (osArch.equals("aarch64") || osArch.equals("arm64")) {
            return "aarch64";
        }

        throw new UnsupportedOperationException("Unsupported architecture: " + osArch);
    }

    private static String getNativeLibraryPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = getArchitecture();

        String osName;
        String libName;

        if (os.contains("win")) {
            osName = "windows";
            libName = "flecs.dll";
        } else if (os.contains("mac")) {
            osName = "macos";
            libName = "libflecs.dylib";
        } else if (os.contains("linux") || os.contains("nux")) {
            osName = "linux";
            libName = "libflecs.so";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }

        return String.format("/natives/%s-%s/%s", osName, arch, libName);
    }

    private static boolean loadFromResources(String libPath) {
        try {
            InputStream in = FlecsLoader.class.getResourceAsStream(libPath);

            if (in == null) {
                // Try fallback to root for backward compatibility
                String libName = libPath.substring(libPath.lastIndexOf('/') + 1);
                in = FlecsLoader.class.getResourceAsStream("/" + libName);

                if (in == null) {
                    return false;
                }
            }

            Path tempLib = Files.createTempFile("flecs", getSuffix(libPath));
            tempLib.toFile().deleteOnExit();

            Files.copy(in, tempLib, StandardCopyOption.REPLACE_EXISTING);
            in.close();

            System.load(tempLib.toAbsolutePath().toString());

            return true;

        } catch (IOException e) {
            return false;
        }
    }

    private static String getSuffix(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx != -1) {
            return filename.substring(idx);
        }
        return "";
    }

    public static boolean isLoaded() {
        return loaded;
    }
}

