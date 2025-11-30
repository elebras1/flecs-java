package com.github.elebras1.flecs.util;

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
            String libName = getNativeLibraryName();

            if (loadFromResources(libName)) {
                loaded = true;
                return;
            }

            System.loadLibrary("flecs");
            loaded = true;

        } catch (Exception e) {
            throw new UnsatisfiedLinkError("Unable to load the native Flecs library: " + e.getMessage());
        }
    }

    private static String getNativeLibraryName() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return "flecs.dll";
        } else if (os.contains("mac")) {
            return "libflecs.dylib";
        } else {
            return "libflecs.so";
        }
    }

    private static boolean loadFromResources(String libName) {
        try {
            String resourcePath = "/" + libName;
            InputStream in = FlecsLoader.class.getResourceAsStream(resourcePath);

            if (in == null) {
                return false;
            }

            Path tempLib = Files.createTempFile("flecs", getSuffix(libName));
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

