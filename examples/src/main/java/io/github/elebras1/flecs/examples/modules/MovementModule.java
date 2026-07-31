package io.github.elebras1.flecs.examples.modules;

import io.github.elebras1.flecs.FlecsModule;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.Velocity;
import io.github.elebras1.flecs.util.Flecs;

public class MovementModule implements FlecsModule {

    @Override
    public void initModule(World world) {
        world.module(this);

        world.component(Position.class);
        world.component(Velocity.class);

        world.system("Move")
                .with(Position.class)
                .with(Velocity.class)
                .kind(Flecs.OnUpdate)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    Field<Velocity> velocities = it.field(Velocity.class, 1);
                    for (int i = 0; i < it.count(); i++) {
                        Position pos = positions.get(i);
                        Velocity vel = velocities.get(i);
                        positions.set(i, new Position(pos.x() + vel.dx(), pos.y() + vel.dy()));
                        System.out.println("p = {" + pos.x() + ", " + pos.y() + "} (system)");
                    }
                });
    }
}
