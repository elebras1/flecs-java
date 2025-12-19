package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.Minister;
import com.github.elebras1.flecs.util.FlecsConstants;

import java.util.Random;

public class MultiThreadedSystemExemple {
    public static void main(String[] args) {
        try (World world = new World()) {
            System.out.println("=== Test Multithreaded system + native access ===");
            world.component(Minister.class);
            world.setThreads(4);

            Random rnd = new Random();
            for (int i = 0; i < 1000; i++) {
                world.obtainEntity(world.entity("Min_" + i)).set(new Minister("M-" + i, "default.png", rnd.nextFloat() * 50, 2020, 0));
            }

            world.system("LoyaltySystem")
                    .with(Minister.class)
                    .kind(FlecsConstants.EcsOnUpdate)
                    .multiThreaded(true)
                    .iter(it -> {
                        int count = it.count();
                        for (int i = 0; i < count; i++) {
                            float loyalty = it.fieldFloat(Minister.class, 0, "loyalty", i);

                            float newLoyalty = Math.min(loyalty + 10.0f, 100.0f);
                            String newImg = newLoyalty > 50 ? "happy.png" : "angry.png";

                            it.setFieldFloat(Minister.class, 0, "loyalty", i, newLoyalty);
                            it.setFieldString(Minister.class, 0, "imageFileName", i, newImg);
                        }
                    });

            for (int f = 0; f < 5; f++) {
                world.progress(0.016f);
            }

            Minister m = world.obtainEntity(world.lookup("Min_42")).get(Minister.class);
            System.out.printf("Check Min_42 -> Loyalty: %.1f | Img: %s%n", m.loyalty(), m.imageFileName());
        }
    }
}