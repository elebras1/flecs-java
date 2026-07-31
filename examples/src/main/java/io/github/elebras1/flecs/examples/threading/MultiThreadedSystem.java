package io.github.elebras1.flecs.examples.threading;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Minister;
import io.github.elebras1.flecs.examples.components.MinisterView;
import io.github.elebras1.flecs.util.Flecs;

import java.util.Locale;
import java.util.Random;

/**
 * Demonstrates running a system across multiple worker threads. The world is
 * configured with World.setThreads(int) and the system is marked as
 * multi-threaded.
 */
public class MultiThreadedSystem {

    static void main(String[] args) {
        World world = new World();
        world.component(Minister.class);
        world.setThreads(4);

        // Fixed seed so the example output is deterministic.
        Random rnd = new Random(12345);
        for (int i = 0; i < 1000; i++) {
            world.obtainEntity(world.entity("Min_" + i))
                    .set(new Minister("M-" + i, "default.png", rnd.nextFloat() * 50, 2020, 0));
        }

        world.system("LoyaltySystem")
                .kind(Flecs.OnUpdate)
                .with(Minister.class)
                .multiThreaded(true)
                .iter(it -> {
                    Field<Minister> ministerField = it.field(Minister.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        MinisterView minister = ministerField.getMutView(i);
                        float newLoyalty = Math.min(minister.loyalty() + 10.0f, 100.0f);
                        String newImg = newLoyalty > 50 ? "happy.png" : "angry.png";
                        minister.loyalty(newLoyalty);
                        minister.imageFileName(newImg);
                    }
                });

        for (int f = 0; f < 5; f++) {
            world.progress(0.016f);
        }

        Minister m = world.obtainEntity(world.lookup("Min_42")).get(Minister.class);
        System.out.printf(Locale.US, "Check Min_42 -> Loyalty: %.1f | Img: %s%n", m.loyalty(), m.imageFileName());

        world.destroy();
    }

    // Output:
    // Check Min_42 -> Loyalty: 97.2 | Img: happy.png
}
