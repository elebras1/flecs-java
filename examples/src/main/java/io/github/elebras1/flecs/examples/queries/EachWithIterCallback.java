package io.github.elebras1.flecs.examples.queries;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.PositionView;
import io.github.elebras1.flecs.examples.components.Velocity;

/**
 * Iterates a query with the lower-level iter callback.
 *
 * <p>Use this style when you need the {@link io.github.elebras1.flecs.Iter}
 * object itself: count, row index, matched entities, component ids, etc.</p>
 */
public class EachWithIterCallback {

    public static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Velocity.class);

        world.obtainEntity(world.entity("e1"))
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));

        world.obtainEntity(world.entity("e2"))
                .set(new Position(10, 20))
                .set(new Velocity(3, 4));

        world.obtainEntity(world.entity("e3"))
                .set(new Position(10, 20));

        Query query = world.query()
                .with(Position.class)
                .with(Velocity.class)
                .build();

        query.iter(it -> {
            Field<Position> positions = it.field(Position.class, 0);
            Field<Velocity> velocities = it.field(Velocity.class, 1);

            for (int i = 0; i < it.count(); i++) {
                long entityId = it.entity(i);
                PositionView pos = positions.getMutView(i);
                Velocity vel = velocities.get(i);

                pos.x(pos.x() + vel.dx());
                pos.y(pos.y() + vel.dy());

                EntityView entity = it.world().obtainEntityView(entityId);
                System.out.println(entity.name() + ": {" + pos.x() + ", " + pos.y() + "}" );
            }
        });

        query.destroy();
        world.destroy();
    }

    // Output:
    // e1: {11.0, 22.0}
    // e2: {13.0, 24.0}
}
