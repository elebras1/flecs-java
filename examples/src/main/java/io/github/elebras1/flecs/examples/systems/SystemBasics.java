package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.FlecsSystem;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.PositionView;
import io.github.elebras1.flecs.examples.components.Velocity;
import io.github.elebras1.flecs.examples.components.VelocityView;

public class SystemBasics {

    static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class);
            world.component(Velocity.class);

            // Create a system for Position, Velocity. Systems are like queries (see
            // queries) with a function that can be ran or scheduled (see pipeline).
            FlecsSystem moveSystem = world.system("MoveSystem")
                    .with(Position.class)
                    .with(Velocity.class)
                    .eachView(Position.class, Velocity.class, (long entityId, PositionView pos, VelocityView vel) -> {
                        pos.x(pos.x() + vel.dx());
                        pos.y(pos.y() + vel.dy());

                        EntityView entity = world.obtainEntityView(entityId);
                        System.out.println(entity.name() + ": {" + pos.x() + ", " + pos.y() + "}");
                    });

            // Create a few test entities for a Position, Velocity query.
            world.obtainEntity(world.entity("e1"))
                    .set(new Position(10, 20))
                    .set(new Velocity(1, 2));

            world.obtainEntity(world.entity("e2"))
                    .set(new Position(10, 20))
                    .set(new Velocity(3, 4));

            // This entity will not match as it does not have Position, Velocity.
            world.obtainEntity(world.entity("e3"))
                    .set(new Position(10, 20));

            // Run the system.
            moveSystem.run();

        }
    }

    // Output:
    // e1: {11.0, 22.0}
    // e2: {13.0, 24.0}
}
