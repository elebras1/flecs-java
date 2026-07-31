package io.github.elebras1.flecs.examples.queries;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Npc;
import io.github.elebras1.flecs.examples.components.Position;

/**
 * Demonstrates adding a component to a query with the "with" method.
 * The component is not part of the query type, so it does not appear in the
 * each/iter function signatures. This is useful for tags.
 */
public class With {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Npc.class);

        // Create a query for Position, Npc. By adding the Npc component using
        // the "with" method, the component is not a part of the query type.
        Query query = world.query().with(Position.class).with(Npc.class).build();

        // Create a few test entities for the Position, Npc query.
        world.obtainEntity(world.entity("e1"))
                .set(new Position(10, 20))
                .add(Npc.class);

        world.obtainEntity(world.entity("e2"))
                .set(new Position(10, 20))
                .add(Npc.class);

        // This entity will not match as it does not have Npc.
        world.obtainEntity(world.entity("e3"))
                .set(new Position(10, 20));

        // Note how the Npc tag is not part of the each signature.
        query.each(Position.class, (entityId, p) -> {
            Entity entity = world.obtainEntity(entityId);
            System.out.println(entity.name() + ": {" + p.x() + ", " + p.y() + "}");
        });

        query.destroy();
        world.destroy();
    }

    // Output:
    // e1: {10.0, 20.0}
    // e2: {10.0, 20.0}
}
