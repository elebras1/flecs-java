package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.examples.components.Position;
import com.github.elebras1.flecs.examples.components.Velocity;
import com.github.elebras1.flecs.examples.components.Health;

public class EntityBasicsExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class);
            world.component(Velocity.class);
            world.component(Health.class);

            long playerTag = world.entity("PlayerTag");
            long enemyTag = world.entity("EnemyTag");

            Entity player = world.obtainEntity(world.entity("Player"));
            player.set(new Position(10, 20));
            player.set(new Velocity(1, 0.5f));
            player.add(playerTag);

            System.out.println("Player has Position: " + player.has(Position.class));
            System.out.println("Player has Health: " + player.has(Health.class));

            Position pos = player.get(Position.class);
            System.out.println("Position: (" + pos.x() + ", " + pos.y() + ")");

            player.set(new Health(100));
            Health health = player.get(Health.class);
            System.out.println("Health: " + health.value());

            player.remove(Velocity.class);
            System.out.println("Player has Velocity: " + player.has(Velocity.class));

            Entity enemy = world.obtainEntity(world.entity("Enemy"));
            enemy.set(new Position(50, 30));
            enemy.set(new Health(50));
            enemy.add(enemyTag);

            enemy.disable(Health.class);
            System.out.println("Enemy Health enabled: " + enemy.enabled(Health.class));

            enemy.enable(Health.class);
            System.out.println("Enemy Health enabled: " + enemy.enabled(Health.class));

            Entity playerClone = player.clone(true);
            playerClone.setName("PlayerClone");
            Position clonePos = playerClone.get(Position.class);
            System.out.println("Clone position: (" + clonePos.x() + ", " + clonePos.y() + ")");

            player.clear();
            System.out.println("Player has Position after clear: " + player.has(Position.class));

            enemy.destruct();
            System.out.println("Enemy is alive: " + enemy.isAlive());
        }
    }
}

