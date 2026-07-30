package io.github.elebras1.flecs.examples;

import io.github.elebras1.flecs.FlecsModule;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.util.Flecs;

public class ModuleExample {

    public static void main(String[] args) {
        World world = new World();

        world.importModule(new MovementModule());

        world.obtainEntity(world.entity("Player")).set(new Position(10, 20));

        world.obtainEntity(world.entity("Enemy")).set(new Position(5, 5));

        world.progress(1);

        world.destroy();
    }

    public static class MovementModule implements FlecsModule {

        @Override
        public void initModule(World world) {
            world.module(this);

            world.component(Position.class);

            world.system().with(Position.class).kind(Flecs.OnUpdate)
                    .each(Position.class, (long entityId, Position position) -> {
                        System.out.printf("%s is at (%.1f, %.1f)%n", world.obtainEntity(entityId).name(), position.x(), position.y());
                    });
        }
    }
}
