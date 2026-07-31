package io.github.elebras1.flecs.examples.systems;

import java.util.Locale;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Timeout;
import io.github.elebras1.flecs.examples.components.TimeoutView;
import io.github.elebras1.flecs.util.Flecs;

/**
 * Demonstrates how to mutate (here, delete) the currently iterated entity from
 * inside a system. The actual deletion is deferred by Flecs until it is safe to
 * apply.
 */
public class MutateEntity {

    public static void main(String[] args) {
        World world = new World();
        world.component(Timeout.class);

        // System that deletes an entity after its timeout expires.
        world.system("Expire")
                .kind(Flecs.OnUpdate)
                .with(Timeout.class)
                .iter(it -> {
                    Field<Timeout> timeouts = it.field(Timeout.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        TimeoutView timeout = timeouts.getMutView(i);
                        timeout.value(timeout.value() - it.deltaTime());
                        if (timeout.value() <= 0) {
                            long entityId = it.entity(i);
                            EntityView entity = it.world().obtainEntityView(entityId);
                            System.out.println("Expire: " + entity.name() + " deleted!");
                            entity.destruct();
                        }
                    }
                });

        // System that prints the remaining expiry time.
        world.system("PrintExpire")
                .kind(Flecs.OnUpdate)
                .with(Timeout.class)
                .iter(it -> {
                    Field<Timeout> timeouts = it.field(Timeout.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        long entityId = it.entity(i);
                        Timeout timeout = timeouts.get(i);
                        EntityView entity = it.world().obtainEntityView(entityId);
                        System.out.printf(Locale.US, "PrintExpire: %s has %.2f seconds left%n", entity.name(), timeout.value());
                    }
                });

        // Observer that triggers when Timeout is actually removed.
        world.observer("ExpiredObserver")
                .event(Flecs.OnRemove)
                .with(Timeout.class)
                .each(entityId -> {
                    EntityView entity = world.obtainEntityView(entityId);
                    System.out.println("Expired: " + entity.name() + " actually deleted");
                });

        Entity entity = world.obtainEntity(world.entity("MyEntity"))
                .set(new Timeout(3.0));

        world.setTargetFps(1);

        while (world.progress()) {
            if (!entity.isAlive()) {
                break;
            }
            System.out.println("Tick...");
        }

        world.destroy();
    }

    // Output:
    // PrintExpire: MyEntity has 2.00 seconds left
    // Tick...
    // PrintExpire: MyEntity has 1.00 seconds left
    // Tick...
    // Expire: MyEntity deleted!
    // PrintExpire: MyEntity has -0.00 seconds left
    // Expired: MyEntity actually deleted
}
