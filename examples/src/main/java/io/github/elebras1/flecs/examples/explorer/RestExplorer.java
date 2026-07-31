package io.github.elebras1.flecs.examples.explorer;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.PositionView;
import io.github.elebras1.flecs.examples.components.Velocity;
import io.github.elebras1.flecs.examples.components.VelocityView;

import java.util.Random;

public class RestExplorer {

    static void main(String[] args) throws InterruptedException {
        World world = new World();
        world.component(Position.class);
        world.component(Velocity.class);

        // Enable the REST interface on port 27750.
        world.enableRest((short) 27750);
        System.out.println("Open https://flecs.dev/explorer?remote=true");

        int numberEntities = 100000;
        long tagTeamA = world.entity("TeamA");
        long tagTeamB = world.entity("TeamB");

        // Create a number of entities tagged with one of two teams and give
        // them a random position and velocity.
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

        // Movement system: update Position from Velocity and bounce off the
        // world bounds.
        world.system("MovementSystem")
                .with(Position.class)
                .with(Velocity.class)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    Field<Velocity> velocities = it.field(Velocity.class, 1);
                    for (int i = 0; i < it.count(); i++) {
                        PositionView p = positions.getMutView(i);
                        VelocityView v = velocities.getMutView(i);
                        float newX = p.x() + v.dx() * it.deltaTime();
                        float newY = p.y() + v.dy() * it.deltaTime();
                        p.x(newX).y(newY);

                        if (newX < 0 || newX > 100) {
                            v.dx(-v.dx()).dy(v.dy());
                        }
                        if (newY < 0 || newY > 100) {
                            velocities.set(i, new Velocity(v.dx(), -v.dy()));
                        }
                    }
                });

        System.out.println("Loop running...");

        // The explorer runs until it is interrupted (e.g. Ctrl+C).
        float deltaTime = 0.016f;
        try {
            while (world.progress(deltaTime)) {
                Thread.sleep(16);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            world.disableRest();
            world.destroy();
        }
    }

    // Output:
    // Open https://flecs.dev/explorer?remote=true
    // Creating entities...
    // Loop running...
}
