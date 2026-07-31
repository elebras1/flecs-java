package io.github.elebras1.flecs.examples.queries;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;

public class Without {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);

        // Tag used to exclude entities from the query.
        long npcTag = world.entity("Npc");

        // Create a few test entities for the Position query.
        world.obtainEntity(world.entity("e1")).set(new Position(10, 20));
        world.obtainEntity(world.entity("e2")).set(new Position(10, 20));

        // This entity will not match because it has the Npc tag.
        world.obtainEntity(world.entity("e3"))
                .set(new Position(10, 20))
                .add(npcTag);

        // Query for entities with Position but without Npc.
        Query query = world.query()
                .with(Position.class)
                .without(npcTag)
                .build();

        query.each(Position.class, (entityId, pos) -> {
            Entity entity = world.obtainEntity(entityId);
            System.out.println(entity.name() + ": {" + pos.x() + ", " + pos.y() + "}");
        });

        query.destroy();
        world.destroy();
    }

    // Output:
    // e1: {10.0, 20.0}
    // e2: {10.0, 20.0}
}
