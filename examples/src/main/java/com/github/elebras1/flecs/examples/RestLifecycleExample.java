package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.World;

public class RestLifecycleExample {

    public static void main(String[] args) throws InterruptedException {
        try (World world = new World()) {
            System.out.println("=== Flecs REST Lifecycle Test ===");

            for(int i = 0; i < 50; i++) {
                System.out.println("[" + i + "] Enabling REST...");
                world.enableRest();
                world.progress(0.016f);
                Thread.sleep(100);
                System.out.println("   -> REST should be active.");
                System.out.println("[" + i + "] Disabling REST...");
                world.disableRest();

                world.progress(0.016f);
                Thread.sleep(100);
                System.out.println("   -> REST should be inactive.");
            }
        }
    }
}