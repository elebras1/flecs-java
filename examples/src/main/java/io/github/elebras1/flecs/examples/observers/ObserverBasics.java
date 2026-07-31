package io.github.elebras1.flecs.examples.observers;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.util.Flecs;

public class ObserverBasics {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);

        // Create an observer for three events.
        world.observer()
                .event(Flecs.OnAdd)
                .event(Flecs.OnRemove)
                .event(Flecs.OnSet)
                .with(Position.class)
                .iter(it -> {
                    long event = it.event();
                    for (int i = 0; i < it.count(); i++) {
                        Entity entity = world.obtainEntity(it.entity(i));
                        if (event == Flecs.OnAdd) {
                            // No assumptions about the component value should be made here. If
                            // a ctor for the component was registered it will be called before
                            // the EcsOnAdd event, but a value assigned by set won't be visible.
                            System.out.println(" - OnAdd: Position: " + entity.name());
                        } else {
                            // Field access is only safe for OnSet/OnRemove.
                            Position p = it.field(Position.class, 0).get(i);
                            String eventName = event == Flecs.OnRemove ? "OnRemove" : "OnSet";
                            System.out.println(" - " + eventName + ": Position: " + entity.name() + ": {" + p.x() + ", " + p.y() + "}");
                        }
                    }
                });

        // Create entity, set Position (emits EcsOnAdd and EcsOnSet).
        Entity e = world.obtainEntity(world.entity("e"))
                .set(new Position(10, 20));

        // Remove component (emits EcsOnRemove).
        e.remove(Position.class);

        // Remove component again (no event is emitted).
        e.remove(Position.class);

        world.destroy();
    }

    // Output:
    //  - OnAdd: Position: e
    //  - OnSet: Position: e: {10.0, 20.0}
    //  - OnRemove: Position: e: {10.0, 20.0}
}
