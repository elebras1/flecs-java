package io.github.elebras1.flecs.examples.prefabs;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Attack;
import io.github.elebras1.flecs.examples.components.Damage;
import io.github.elebras1.flecs.examples.components.Defense;
import io.github.elebras1.flecs.util.Flecs;

/**
 * Demonstrates prefab component override behavior. Components can be configured
 * to be inherited from the prefab instead of copied to the instance, and
 * instances can still override them explicitly.
 */
public class PrefabOverride {

    public static void main(String[] args) {
        World world = new World();

        // Configure Attack and Defense to be inherited from prefabs.
        long attackId = world.component(Attack.class);
        long defenseId = world.component(Defense.class);
        long damageId = world.component(Damage.class);

        world.obtainEntity(attackId).addRelation(Flecs.OnInstantiate, Flecs.Inherit);
        world.obtainEntity(defenseId).addRelation(Flecs.OnInstantiate, Flecs.Inherit);

        // Create a SpaceShip prefab.
        Entity spaceShip = world.obtainEntity(world.entity("SpaceShip"))
                .add(Flecs.Prefab)
                .set(new Attack(75))
                .set(new Defense(100))
                .set(new Damage(50));

        // Create a prefab instance.
        Entity inst = world.obtainEntity(world.entity("my_spaceship"))
                .isA(spaceShip.id());

        // The instance has a private copy of Damage, but Attack and Defense are
        // shared from the prefab.
        System.out.println(inst.table().str());

        // Override Attack explicitly on the instance.
        inst.add(Attack.class);
        System.out.println(inst.table().str());

        // Values can be read whether they are inherited or overridden.
        System.out.println("attack: " + inst.get(Attack.class).value());
        System.out.println("defense: " + inst.get(Defense.class).value());
        System.out.println("damage: " + inst.get(Damage.class).value());

        world.destroy();
    }

    // Output:
    // Damage,(Identifier,Name),(IsA,SpaceShip)
    // Attack,Damage,(Identifier,Name),(IsA,SpaceShip)
    // attack: 75.0
    // defense: 100.0
    // damage: 50.0
}
