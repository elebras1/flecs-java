package io.github.elebras1.flecs.examples.queries;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.DummyTag;
import io.github.elebras1.flecs.examples.components.Group;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.PositionView;

/**
 * Demonstrates grouped queries. Entities with the same group id are stored
 * together and iterated together. Groups are ordered by group id, which gives
 * a coarse-grained ordering that is cheaper to maintain than per-entity sort.
 */
public class GroupBy {

    public static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Group.class);
        world.component(DummyTag.class);

        // Targets for the grouping relationship. Create them in order so that
        // their ids reflect the intended group ordering.
        long first = world.entity("First");
        long second = world.entity("Second");
        long third = world.entity("Third");

        // Build a grouped query.
        Query query = world.query()
                .with(Position.class)
                .groupBy(Group.class)
                .build();

        // Create entities in three different groups and two different tables.
        Entity e1 = world.obtainEntity(world.entity())
                .set(new Position(1, 1));
        e1.addRelation(world.component(Group.class), third);

        Entity e2 = world.obtainEntity(world.entity())
                .set(new Position(2, 2));
        e2.addRelation(world.component(Group.class), second);

        Entity e3 = world.obtainEntity(world.entity())
                .set(new Position(3, 3));
        e3.addRelation(world.component(Group.class), first);

        Entity e4 = world.obtainEntity(world.entity())
                .set(new Position(4, 4))
                .add(DummyTag.class);
        e4.addRelation(world.component(Group.class), third);

        Entity e5 = world.obtainEntity(world.entity())
                .set(new Position(5, 5))
                .add(DummyTag.class);
        e5.addRelation(world.component(Group.class), second);

        Entity e6 = world.obtainEntity(world.entity())
                .set(new Position(6, 6))
                .add(DummyTag.class);
        e6.addRelation(world.component(Group.class), first);

        // Iterate the query. Entities are returned grouped by their group id.
        query.iter(it -> {
            Field<Position> positions = it.field(Position.class, 0);
            for (int i = 0; i < it.count(); i++) {
                PositionView pos = positions.getMutView(i);
                System.out.println("{" + pos.x() + ", " + pos.y() + "}");
            }
            System.out.println();
        });

        query.destroy();
        world.destroy();
    }

    // Output:
    // {3.0, 3.0}
    //
    // {6.0, 6.0}
    //
    // {2.0, 2.0}
    //
    // {5.0, 5.0}
    //
    // {1.0, 1.0}
    //
    // {4.0, 4.0}
    //
}
