package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.FlecsSystem;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.PositionView;
import io.github.elebras1.flecs.examples.components.Velocity;

public class SystemBasics {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Velocity.class);

        // Create a system for Position, Velocity. Systems are like queries (see
        // queries) with a function that can be ran or scheduled (see pipeline).
        FlecsSystem moveSystem = world.system("MoveSystem")
                .with(Position.class)
                .with(Velocity.class)
                .each(Position.class, (entityId, pos) -> {
                    // Note: this simple callback only receives Position.
                    // The Velocity component is read from the entity directly.
                    EntityView entity = world.obtainEntityView(entityId);
                    Velocity vel = entity.get(Velocity.class);
                    entity.set(Position.class, (PositionView posView) -> {
                        posView.x(pos.x() + vel.dx());
                        posView.y(pos.y() + vel.dy());
                    });
                    Position newPos = entity.get(Position.class);
                    System.out.println(entity.name() + ": {" + newPos.x() + ", " + newPos.y() + "}");
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

        world.destroy();
    }

    // Output:
    // e1: {11.0, 22.0}
    // e2: {13.0, 24.0}
}
