package io.github.elebras1.flecs.examples.queries;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.PositionView;
import io.github.elebras1.flecs.examples.components.Velocity;
import io.github.elebras1.flecs.examples.components.VelocityView;

/**
 * Same as EachCallback, but the callback also receives the matched entity id.
 *
 * <p>Use this overload when you need the entity id (for example, to print the
 * entity name) while mutating components in place.</p>
 */
public class EachWithEntityCallback {

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

        query.eachView(Position.class, Velocity.class, (long entityId, PositionView pos, VelocityView vel) -> {
            pos.x(pos.x() + vel.dx());
            pos.y(pos.y() + vel.dy());
            EntityView entity = world.obtainEntityView(entityId);
            System.out.println(entity.name() + ": {" + pos.x() + ", " + pos.y() + "}" );
        });

        query.destroy();
        world.destroy();
    }

    // Output:
    // e1: {11.0, 22.0}
    // e2: {13.0, 24.0}
}
