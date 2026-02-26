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

            for (int i = 0; i < 10; i++) {
                long entityId = world.entity("Entity_" + i);
                EntityView entityView = world.obtainEntityView(entityId);
                int finalI = i;
                entityView.set(Position.class, (PositionView positionView) ->
                        positionView.x(finalI * 10.0f).y(finalI * 5.0f));
                entityView.set(Velocity.class, (VelocityView velocityView) ->
                        velocityView.dx(1.0f).dy(0.5f));
                entityView.set(Inventory.class, (InventoryView inventoryView) -> {
                    for(int j = 0; j < inventoryView.elementsLength(); j++) {
                        inventoryView.elements(j, j);
                    }
                });
            }

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

