package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.PositionView;
import io.github.elebras1.flecs.examples.components.Velocity;
import io.github.elebras1.flecs.examples.components.VelocityView;
import io.github.elebras1.flecs.Flecs;

/**
 * Demonstrates the built-in pipeline. Systems are assigned to phases such as
 * Flecs.OnUpdate and Flecs.PostUpdate. Calling
 * World.progress(float) runs all systems ordered by their phase.
 * Systems within the same phase run in declaration order.
 */
public class Pipeline {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Velocity.class);

        // Create a system for moving an entity.
        world.system("Move")
                .with(Position.class)
                .with(Velocity.class)
                .kind(Flecs.OnUpdate)
                .eachView(Position.class, Velocity.class, (PositionView p, VelocityView v) -> {
                    p.x(p.x() + v.dx());
                    p.y(p.y() + v.dy());
                });

        // Create a system for printing the entity position.
        world.system("PrintPosition")
                .with(Position.class)
                .kind(Flecs.PostUpdate)
                .eachView(Position.class, (long entityId, PositionView p) -> {
                    EntityView entity = world.obtainEntityView(entityId);
                    System.out.println(entity.name() + ": {" + p.x() + ", " + p.y() + "}");
                });

        // Create a few test entities for a Position, Velocity query.
        world.obtainEntity(world.entity("e1"))
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));

        world.obtainEntity(world.entity("e2"))
                .set(new Position(10, 20))
                .set(new Velocity(3, 4));

        // Run the default pipeline once.
        world.progress(0.016f);

        world.destroy();
    }

    // Output:
    // e1: {11.0, 22.0}
    // e2: {13.0, 24.0}
}
