package io.github.elebras1.flecs.examples.observers;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.Flecs;

public class ObserverBasics {

    static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class);

            // Create an observer for three events.
            world.observer()
                    .event(Flecs.OnAdd)
                    .event(Flecs.OnRemove)
                    .event(Flecs.OnSet)
                    .with(Position.class)
                    .each(Position.class, (Iter it, int index, Position p) -> {
                        long event = it.event();
                        Entity entity = world.obtainEntity(it.entity(index));
                        if (event == Flecs.OnAdd) {
                            // No assumptions about the component value should be made here. If
                            // a ctor for the component was registered it will be called before
                            // the EcsOnAdd event, but a value assigned by set won't be visible.
                            System.out.println(" - OnAdd: Position: " + entity.name());
                        } else {
                            // Component access is only safe for OnSet/OnRemove.
                            String eventName = event == Flecs.OnRemove ? "OnRemove" : "OnSet";
                            System.out.println(" - " + eventName + ": Position: " + entity.name() + ": {" + p.x() + ", " + p.y() + "}");
                        }
                    });

            // Create entity, set Position (emits EcsOnAdd and EcsOnSet).
            Entity e = world.obtainEntity(world.entity("e"))
                    .set(new Position(10, 20));

            // Remove component (emits EcsOnRemove).
            e.remove(Position.class);

            // Remove component again (no event is emitted).
            e.remove(Position.class);

        }
    }

    // Output:
    //  - OnAdd: Position: e
    //  - OnSet: Position: e: {10.0, 20.0}
    //  - OnRemove: Position: e: {10.0, 20.0}
}
