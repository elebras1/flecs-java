package io.github.elebras1.flecs.examples.observers;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Click;
import io.github.elebras1.flecs.examples.components.Resize;

/**
 * Demonstrates per-entity event observers and enqueuing events on an entity.
 * Events emitted while the world is deferred are processed when the queue is
 * flushed.
 */
public class EnqueueEntityEvent {

    public static void main(String[] args) {
        World world = new World();
        long clickId = world.component(Click.class);
        long resizeId = world.component(Resize.class);

        Entity widget = world.obtainEntity(world.entity("MyWidget"));

        // Observer without source argument.
        widget.observe(clickId, () -> System.out.println("clicked!"));

        // Observer with source argument (the entity itself).
        widget.observe(clickId, () -> System.out.println("clicked on " + widget.name() + "!"));

        // Observer for an event with payload.
        widget.observe(Resize.class, resize ->
                System.out.println("resized to {" + resize.width() + ", " + resize.height() + "}!"));

        // Emit events while deferred.
        world.deferBegin();

        widget.emit(clickId);

        // For a payload event, set the component on the entity and emit the event.
        widget.set(new Resize(100, 200));
        widget.emit(resizeId, Resize.class);

        System.out.println("Events enqueued!");
        world.deferEnd();

        world.destroy();
    }

    // Output:
    // Events enqueued!
    // clicked!
    // clicked on MyWidget!
    // resized to {100.0, 200.0}!
}
