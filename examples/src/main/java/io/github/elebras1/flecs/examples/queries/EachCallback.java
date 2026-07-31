package io.github.elebras1.flecs.examples.queries;

import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.PositionView;
import io.github.elebras1.flecs.examples.components.Velocity;
import io.github.elebras1.flecs.examples.components.VelocityView;

/**
 * Iterates a query with an eachView callback and mutates components in place.
 *
 * <p>eachView gives the callback direct component views, which lets you modify
 * the ECS storage without allocating new component records.</p>
 */
public class EachCallback {

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

        // This entity will not match as it does not have Position, Velocity.
        world.obtainEntity(world.entity("e3"))
                .set(new Position(10, 20));

        Query query = world.query()
                .with(Position.class)
                .with(Velocity.class)
                .build();

        query.eachView(Position.class, Velocity.class, (PositionView pos, VelocityView vel) -> {
            pos.x(pos.x() + vel.dx());
            pos.y(pos.y() + vel.dy());
            System.out.println("{" + pos.x() + ", " + pos.y() + "}");
        });

        query.destroy();
        world.destroy();
    }

    // Output:
    // {11.0, 22.0}
    // {13.0, 24.0}
}
