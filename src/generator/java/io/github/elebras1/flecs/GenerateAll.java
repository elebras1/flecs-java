package io.github.elebras1.flecs;

public class GenerateAll {

    private GenerateAll() {
    }

    public static void main(String[] args) {
        CallbackGenerator.main(args);
        IterationBaseGenerator.main(args);
        EntityBaseGenerator.main(args);
        WorldBaseGenerator.main(args);
    }
}