package io.github.elebras1.flecs.examples.modules;

import io.github.elebras1.flecs.FlecsModule;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.Velocity;
import io.github.elebras1.flecs.examples.components.VelocityView;
import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.examples.components.PositionMutView;

public class MovementModule implements FlecsModule {

    @Override
    public void initModule(World world) {
        world.module(this);

        world.component(Position.class);
        world.component(Velocity.class);

        world.system("Move", Position.class, Velocity.class)
                .kind(Flecs.OnUpdate)
                .eachView(Position.class, Velocity.class, (PositionMutView pos, VelocityView vel) -> {
                    System.out.println("p = {" + pos.x() + ", " + pos.y() + "} (system)");
                    pos.x(pos.x() + vel.dx()).y(pos.y() + vel.dy());
                });
    }
}
