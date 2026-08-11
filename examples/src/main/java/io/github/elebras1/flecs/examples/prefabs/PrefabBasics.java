package io.github.elebras1.flecs.examples.prefabs;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Defense;
import io.github.elebras1.flecs.util.Flecs;

public class PrefabBasics {

    static void main(String[] args) {
        World world = new World();
        world.component(Defense.class);

        // Make Defense component inheritable. By default components are copied from
        // the prefab to the instance. An inherited component is only stored on the
        // prefab, and is shared across all instances.
        long defenseId = world.component(Defense.class);
        Entity defenseEntity = world.obtainEntity(defenseId);
        defenseEntity.add(Flecs.OnInstantiate, Flecs.Inherit);
        // The line above configures the component's OnInstantiate policy.
        // In the Java binding this is done by adding the (OnInstantiate, Inherit)
        // pair to the component entity.

        // Create a SpaceShip prefab with a Defense component.
        Entity spaceShip = world.obtainEntity(world.entity("SpaceShip"))
                .add(Flecs.Prefab)
                .set(new Defense(50));

        // Create a prefab instance.
        Entity inst = world.obtainEntity(world.entity("my_spaceship"))
                .isA(spaceShip.id());

        // Because of the IsA relationship, the instance now shares the Defense
        // component with the prefab, and can be retrieved as a regular component.
        Defense dInst = inst.get(Defense.class);
        System.out.println("defense: " + dInst.value());

        // Because the component is shared, changing the value on the prefab will
        // also change the value for the instance.
        spaceShip.set(new Defense(100));
        Defense dInstAfter = inst.get(Defense.class);
        System.out.println("defense after set: " + dInstAfter.value());

        // Prefab components can be iterated like regular components.
        Query query = world.query().with(Defense.class).build();
        query.each(entityId -> {
            Entity entity = world.obtainEntity(entityId);
            Defense d = entity.get(Defense.class);
            System.out.println((entity.name() != null ? entity.name() : "") + ": " + d.value());
        });
        query.destroy();

        world.destroy();
    }

    // Output:
    // defense: 50.0
    // defense after set: 100.0
    // my_spaceship: 100.0
}
