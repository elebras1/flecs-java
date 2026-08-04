package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.PositionView;
import io.github.elebras1.flecs.examples.components.Velocity;
import io.github.elebras1.flecs.examples.components.VelocityView;
import io.github.elebras1.flecs.util.Flecs;

/**
 * Demonstrates sync points. While systems are progressing, operations such as
 * {@code set} are deferred until it is safe to merge. By default this merge
 * happens at the end of the frame, but the scheduler can insert sync points
 * earlier when systems annotate that they write components not provided by
 * their signature.
 */
public class SyncPoint {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Velocity.class);

        // System that sets Velocity using a component view for entities with
        // Position. Because this operation is not writing to a matched component,
        // it is deferred and causes a sync point to be inserted.
        world.system("SetVelocity")
                .kind(Flecs.OnUpdate)
                .with(Position.class)
                .iter(it -> {
                    for (int i = 0; i < it.count(); i++) {
                        long entityId = it.entity(i);
                        world.obtainEntity(entityId).insert(Velocity.class, (VelocityView v) -> {
                            v.dx(1);
                            v.dy(2);
                        });
                    }
                });

        // This system reads Velocity, which causes the insertion of a sync point.
        world.system("Move")
                .kind(Flecs.OnUpdate)
                .with(Position.class)
                .with(Velocity.class)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    Field<Velocity> velocities = it.field(Velocity.class, 1);
                    for (int i = 0; i < it.count(); i++) {
                        PositionView p = positions.getMutView(i);
                        Velocity v = velocities.get(i);
                        p.x(p.x() + v.dx());
                        p.y(p.y() + v.dy());
                    }
                });

        // Print resulting Position.
        world.system("PrintPosition")
                .with(Position.class)
                .kind(Flecs.PostUpdate)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        long entityId = it.entity(i);
                        Position p = positions.get(i);
                        System.out.println(world.obtainEntity(entityId).name() + ": {" + p.x() + ", " + p.y() + "}");
                    }
                });

        // Create a few test entities for a Position, Velocity query.
        world.obtainEntity(world.entity("e1"))
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));

        world.obtainEntity(world.entity("e2"))
                .set(new Position(10, 20))
                .set(new Velocity(3, 4));

        // Run systems once.
        world.progress(0.016f);

        world.destroy();
    }

    // Output:
    // e1: {11.0, 22.0}
    // e2: {11.0, 22.0}
}
