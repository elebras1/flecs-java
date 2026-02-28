package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Query;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.examples.components.Health;

public class DeferredOperationsExample {

    public static void main(String[] args) {
        try(World world = new World()) {
            world.component(Health.class);

            for(int i = 0; i < 1000; i++) {
                Entity entity = world.obtainEntity(world.entity("entity_" + i));
                entity.set(new Health(i));
            }

            Query query = world.query().with(Health.class).build();

            world.deferBegin();
            for(int i = 10; i < 20; i++) {
                long entityId = world.lookup("entity_" + i);
                Entity entity = world.obtainEntity(entityId);
                entity.remove(Health.class);
            }
            if(query.count() != 1000) {
                throw new RuntimeException("Expected 1000 entities, but got " + query.count());
            }
            world.deferEnd();

            if(query.count() != 990) {
                throw new RuntimeException("Expected 990 entities, but got " + query.count());
            }

            query.close();
        }
    }
}
