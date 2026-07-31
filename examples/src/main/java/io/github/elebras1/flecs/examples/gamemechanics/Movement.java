package io.github.elebras1.flecs.examples.gamemechanics;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.PositionView;
import io.github.elebras1.flecs.examples.components.Velocity;
import io.github.elebras1.flecs.util.Flecs;

public class Movement {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Velocity.class);

        // Scene root.
        Entity scene = world.obtainEntity(world.entity("Scene"));

        // Player and enemy are children of the scene.
        world.setScope(scene.id());
        Entity player = world.obtainEntity(world.entity("Player"))
                .set(new Position(0, 0))
                .set(new Velocity(1, 0));
        Entity enemy = world.obtainEntity(world.entity("Enemy"))
                .set(new Position(10, 0))
                .set(new Velocity(-0.5f, 0));
        world.setScope(0);

        // Simple movement system.
        world.system("Move")
                .with(Position.class)
                .with(Velocity.class)
                .kind(Flecs.OnUpdate)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    Field<Velocity> velocities = it.field(Velocity.class, 1);
                    for (int i = 0; i < it.count(); i++) {
                        PositionView pos = positions.getMutView(i);
                        Velocity vel = velocities.get(i);
                        pos.x(pos.x() + vel.dx());
                        pos.y(pos.y() + vel.dy());
                        EntityView entity = it.world().obtainEntityView(it.entity(i));
                        System.out.println(entity.name() + ": {" + pos.x() + ", " + pos.y() + "}");
                    }
                });

        // Simulate a few frames.
        for (int i = 0; i < 3; i++) {
            System.out.println("== Frame " + (i + 1) + " ==");
            world.progress(1.0f);
        }

        world.destroy();
    }

    // Output:
    // == Frame 1 ==
    // Player: {1.0, 0.0}
    // Enemy: {9.5, 0.0}
    // == Frame 2 ==
    // Player: {2.0, 0.0}
    // Enemy: {9.0, 0.0}
    // == Frame 3 ==
    // Player: {3.0, 0.0}
    // Enemy: {8.5, 0.0}
}
