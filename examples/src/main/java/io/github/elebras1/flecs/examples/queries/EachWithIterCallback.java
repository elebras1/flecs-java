package io.github.elebras1.flecs.examples.queries;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.Velocity;
import io.github.elebras1.flecs.examples.components.VelocityView;
import io.github.elebras1.flecs.examples.components.PositionMutView;

/**
 * Iterates a query with the lower-level iter callback.
 *
 * <p>Use this style when you need the {@link io.github.elebras1.flecs.Iter}
 * object itself: count, row index, matched entities, component ids, etc.</p>
 */
public class EachWithIterCallback {

    public static void main(String[] args) {
        try (World world = new World()) {
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

            query.eachView(Position.class, Velocity.class, (Iter it, int index, PositionMutView pos, VelocityView vel) -> {
                pos.x(pos.x() + vel.dx());
                pos.y(pos.y() + vel.dy());

                EntityView entity = it.world().obtainEntityView(it.entity(index));
                System.out.println(entity.name() + ": {" + pos.x() + ", " + pos.y() + "}" );
            });

        }
    }

    // Output:
    // e1: {11.0, 22.0}
    // e2: {13.0, 24.0}
}
