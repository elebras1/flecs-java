package io.github.elebras1.flecs.examples.modules;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.Velocity;

/**
 * Demonstrates importing a module. The module definition is in
 * MovementModule. In a real project, MovementModule would
 * typically be public.
 */
public class SimpleModule {

    static void main(String[] args) {
        World world = new World();

        // Import the module.
        world.importModule(new MovementModule());

        // Create entity with imported components.
        Entity e = world.obtainEntity(world.entity("e"))
                .set(new Position(10, 20))
                .set(new Velocity(1, 1));

        // Call progress which runs the imported Move system.
        world.progress(0.0f);

        // Use component from module in operation.
        Position p = e.get(Position.class);
        System.out.println("p = {" + p.x() + ", " + p.y() + "} (get)");

        world.destroy();
    }

    // Output (system prints the value before the update):
    // p = {10.0, 20.0} (system)
    // p = {11.0, 21.0} (get)
}
