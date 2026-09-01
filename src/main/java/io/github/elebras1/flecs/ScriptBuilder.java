package io.github.elebras1.flecs;

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

    public long run() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment desc = ecs_script_desc_t.allocate(arena);
            if (this.name != null) {
                ecs_script_desc_t.filename(desc, arena.allocateFrom(this.name));
            }
            if (this.code != null) {
                ecs_script_desc_t.code(desc, arena.allocateFrom(this.code));
            }

            long scriptId = flecs_h.ecs_script_init(this.world.worldSeg(), desc);
            if (scriptId == 0) {
                throw new RuntimeException("Flecs script execution failed. Check console for parsing errors.");
            }
            return scriptId;
        }
    }

    public void update(Entity script) {
        this.update(script, this.code);
    }

    public void update(Entity script, String newCode) {
        try (Arena arena = Arena.ofConfined()) {
            int result = flecs_h.ecs_script_update(this.world.worldSeg(), script.id(), 0, arena.allocateFrom(newCode));
            if (result != 0) {
                throw new RuntimeException("Flecs script update failed. Check console for parsing errors.");
            }
        }
    }

}
