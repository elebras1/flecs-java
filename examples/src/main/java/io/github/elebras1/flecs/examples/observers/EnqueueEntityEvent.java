package io.github.elebras1.flecs.examples.observers;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Click;
import io.github.elebras1.flecs.examples.components.Resize;

/**
 * Demonstrates per-entity event observers and enqueuing events on an entity.
 * Events enqueued on a stage are processed when the stage is merged.
 */
public class EnqueueEntityEvent {

    public static void main(String[] args) {
        try (World world = new World()) {
            long clickId = world.component(Click.class);
            world.component(Resize.class);

            Entity widget = world.obtainEntity(world.entity("MyWidget"));

            // Observer without source argument.
            widget.observe(clickId, () -> System.out.println("clicked!"));

            // Observer with source argument (the entity itself).
            widget.observe(clickId, () -> System.out.println("clicked on " + widget.name() + "!"));

            // Observer for an event with payload.
            widget.observe(Resize.class, resize ->
                    System.out.println("resized to {" + resize.width() + ", " + resize.height() + "}!"));

            // Enqueue events on a stage.
            World stage = world.getStage(0);
            Entity stageWidget = stage.obtainEntity(widget.id());

            stageWidget.enqueue(clickId);

            // For a payload event, enqueue the payload with the event.
            stageWidget.enqueue(Resize.class, new Resize(100, 200));

            System.out.println("Events enqueued!");

            // Flushes the queue and invokes the observers.
            stage.merge();

        }
    }

    // Output:
    // Events enqueued!
    // clicked!
    // clicked on MyWidget!
    // resized to {100.0, 200.0}!
}
