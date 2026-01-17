package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.examples.components.*;
import com.github.elebras1.flecs.util.FlecsConstants;

public class ViewExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class);
            world.component(Velocity.class);
            world.component(Inventory.class);

            world.scope(() -> {
                for (int i = 0; i < 10; i++) {
                    long entityId = world.entity("Entity_" + i);
                    EntityView entityView = world.obtainEntityView(entityId);
                    entityView.set(new Position(i * 10.0f, i * 5.0f));
                    entityView.set(new Velocity(1.0f, 0.5f));
                    int[] elements = new int[10];
                    for(int j = 0; j < elements.length; j++) {
                        elements[j] = j;
                    }
                    entityView.set(new Inventory(elements));
                }
            });

            world.system("MovementSystem").kind(FlecsConstants.EcsOnUpdate).with(Position.class).with(Velocity.class).multiThreaded().each(entityId -> {
                EntityView entityView = world.obtainEntityView(entityId);

                PositionView posView = entityView.getMutView(Position.class);
                VelocityView velView = entityView.getMutView(Velocity.class);
                InventoryView invView = entityView.getMutView(Inventory.class);

                posView.x(posView.x() + velView.dx()).y(posView.y() + velView.dy());

                for(int i = 0; i < invView.elementsLength(); i++) {
                    invView.elements(i, invView.elements(i) + 1);
                }
            });

            world.progress(0.016f);
            world.progress(0.016f);
        }
    }
}

