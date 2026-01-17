package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.examples.components.Position;
import com.github.elebras1.flecs.examples.components.Health;
import com.github.elebras1.flecs.examples.components.Velocity;

public class PrefabExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class);
            world.component(Health.class);
            world.component(Velocity.class);

            long enemyPrefabId = world.prefab();
            Entity enemyPrefab = world.obtainEntity(enemyPrefabId);
            enemyPrefab.setName("EnemyPrefab");
            enemyPrefab.set(new Position(0, 0));
            enemyPrefab.set(new Health(50));
            enemyPrefab.set(new Velocity(1, 0));

            Entity enemy1 = world.obtainEntity(world.entity("Enemy1"));
            enemy1.isA(enemyPrefabId);

            Health health1 = enemy1.get(Health.class);
            Position pos1 = enemy1.get(Position.class);
            System.out.println("Enemy1 health: " + health1.value());
            System.out.println("Enemy1 position: (" + pos1.x() + ", " + pos1.y() + ")");

            enemy1.set(new Position(100, 50));
            Position newPos1 = enemy1.get(Position.class);
            System.out.println("Enemy1 overridden position: (" + newPos1.x() + ", " + newPos1.y() + ")");

            Health health2 = enemy1.get(Health.class);
            System.out.println("Enemy1 inherited health: " + health2.value());

            Entity enemy2 = world.obtainEntity(world.entity("Enemy2"));
            enemy2.isA(enemyPrefabId);
            enemy2.set(new Health(75));

            Health health3 = enemy2.get(Health.class);
            System.out.println("Enemy2 overridden health: " + health3.value());

            System.out.println("Enemy1 owns Position: " + enemy1.owns(Position.class));
            System.out.println("Enemy1 owns Health: " + enemy1.owns(Health.class));
        }
    }
}

