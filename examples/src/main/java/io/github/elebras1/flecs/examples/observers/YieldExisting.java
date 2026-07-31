package io.github.elebras1.flecs.examples.observers;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.util.Flecs;

/**
 * Demonstrates observers that trigger for entities already matching the query
 * when the observer is created. This is useful for processing existing state.
 */
public class YieldExisting {

    public static void main(String[] args) {
        World world = new World();
        world.component(Position.class);

        // Create entities before the observer exists.
        world.obtainEntity(world.entity("e1")).set(new Position(10, 20));
        world.obtainEntity(world.entity("e2")).set(new Position(20, 30));

        // Create an observer that fires once for every existing match.
        world.observer()
                .event(Flecs.OnSet)
                .with(Position.class)
                .yieldExisting()
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        Position p = positions.get(i);
                        System.out.println(" - OnSet: Position: " + world.obtainEntity(it.entity(i)).name() + ": {" + p.x() + ", " + p.y() + "}");
                    }
                });

        world.destroy();
    }

    // Output:
    //  - OnSet: Position: e1: {10.0, 20.0}
    //  - OnSet: Position: e2: {20.0, 30.0}
}
