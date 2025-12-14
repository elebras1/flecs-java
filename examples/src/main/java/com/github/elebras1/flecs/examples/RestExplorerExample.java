package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.*;
import java.util.Random;

public class RestExplorerExample {

    public static void main(String[] args) throws InterruptedException {
        try (Flecs world = new Flecs()) {
            flecs_h.ecs_log_set_level(1);
            System.out.println("=== Flecs REST Explorer Example ===");
            FlecsServer server = world.restServer((short) 27750);
            System.out.println("Open https://flecs.dev/explorer?remote=true");

            int numberEntities = 1000;
            world.component(Position.class);
            world.component(Velocity.class);
            long tagTeamA = world.entity("TeamA");
            long tagTeamB = world.entity("TeamB");

            System.out.println("Creating entities...");
            Random rand = new Random();
            for (int i = 0; i < numberEntities; i++) {
                String name = "Unit_" + i;
                long team = (i % 2 == 0) ? tagTeamA : tagTeamB;
                world.obtainEntity(world.entity(name))
                        .add(team)
                        .set(new Position(rand.nextFloat() * 100, rand.nextFloat() * 100))
                        .set(new Velocity((rand.nextFloat() - 0.5f) * 10, (rand.nextFloat() - 0.5f) * 10));
            }

            world.system("MovementSystem")
                    .with(Position.class).with(Velocity.class)
                    .iter(it -> {
                        for (int i = 0; i < it.count(); i++) {
                            long entityId = it.entity(i);
                            Entity e = world.obtainEntity(entityId);
                            Position p = e.get(Position.class);
                            Velocity v = e.get(Velocity.class);

                            float newX = p.x() + v.dx() * it.deltaTime();
                            float newY = p.y() + v.dy() * it.deltaTime();
                            if (newX < 0 || newX > 100) e.set(new Velocity(-v.dx(), v.dy()));
                            if (newY < 0 || newY > 100) e.set(new Velocity(v.dx(), -v.dy()));

                            e.set(new Position(newX, newY));
                        }
                    });

            System.out.println("Loop running...");

            float deltaTime = 0.016f;
            while (world.progress(deltaTime)) {
                world.processHttp(server, deltaTime);

                Thread.sleep(16);
            }

            world.restServerStop(server);
        }
    }
}