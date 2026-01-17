package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.examples.components.*;
import com.github.elebras1.flecs.util.FlecsConstants;

public class ComponentViewExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class);
            world.component(Velocity.class);
            world.component(Inventory.class);

            for (int i = 0; i < 10; i++) {
                long entityId = world.entity("Entity_" + i);
                Entity entity = world.obtainEntity(entityId);
                entity.set(new Position(i * 10.0f, i * 5.0f));
                entity.set(new Velocity(1.0f, 0.5f));
                int[] elements = new int[10];
                for(int j = 0; j < elements.length; j++) {
                    elements[j] = j;
                }
                entity.set(new Inventory(elements));
            }

            world.system("MovementSystem").kind(FlecsConstants.EcsOnUpdate).with(Position.class).with(Velocity.class).multiThreaded().each(entityId -> {
                Entity entity = world.obtainEntity(entityId);

                PositionComponentView positionView = entity.getMutView(Position.class);
                VelocityComponentView velocityView = entity.getMutView(Velocity.class);
                InventoryComponentView inventoryView = entity.getMutView(Inventory.class);

                float currentX = positionView.x();
                float currentY = positionView.y();
                float dx = velocityView.dx();
                float dy = velocityView.dy();
                positionView.x(currentX + dx).y(currentY + dy);

                for(int i = 0; i < inventoryView.elementsLength(); i++) {
                    int element = inventoryView.elements(i);
                    inventoryView.elements(i, element + 1);
                }

                System.out.printf("Entity %d moved to position (%.2f, %.2f) with inventory: ", entityId, positionView.x(), positionView.y());
                for(int i = 0; i < inventoryView.elementsLength(); i++) {
                    System.out.print(inventoryView.elements(i) + "\n");
                }
            });

            world.progress(0.016f);
            world.progress(0.016f);
        }
    }
}

