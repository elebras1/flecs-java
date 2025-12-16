package com.github.elebras1.flecs;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScriptBuilder {
    private final World world;
    private final String name;
    private final String code;

    public ScriptBuilder(World world, String code) {
        this(world, null, code);
    }

    private ScriptBuilder(World world, String name, String code) {
        this.world = world;
        this.name = name;
        this.code = code;
    }

    public ScriptBuilder name(String name) {
        return new ScriptBuilder(this.world, name, this.code);
    }

    public ScriptBuilder code(String code) {
        return new ScriptBuilder(this.world, this.name, code);
    }

    public ScriptBuilder filename(String path) {
        try {
            if(path.toLowerCase().endsWith(".flecs")) {
                String loadedCode = Files.readString(Path.of(path));
                String scriptName = (this.name != null) ? this.name : path;
                return new ScriptBuilder(this.world, scriptName, loadedCode);
            }
            throw new IllegalArgumentException("Unsupported script file extension: " + path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read script file: " + path, e);
        }
    }

    public void run() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment nameSeg = (this.name != null) ? arena.allocateFrom(this.name) : MemorySegment.NULL;
            MemorySegment codeSeg = (this.code != null) ? arena.allocateFrom(this.code) : MemorySegment.NULL;
            int result = flecs_h.ecs_script_run(this.world.nativeHandle(), nameSeg, codeSeg, MemorySegment.NULL);
            if (result != 0) {
                throw new RuntimeException("Flecs script execution failed. Check console for parsing errors.");
            }
        }
    }

}
