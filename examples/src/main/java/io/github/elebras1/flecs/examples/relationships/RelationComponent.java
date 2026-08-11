package io.github.elebras1.flecs.examples.relationships;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Id;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Expires;
import io.github.elebras1.flecs.examples.components.Gigawatts;
import io.github.elebras1.flecs.examples.components.MustHave;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.Requires;
import io.github.elebras1.flecs.examples.components.RequiresView;
import io.github.elebras1.flecs.util.Flecs;

/**
 * Shows how relationships can carry data. When one element of a pair is a
 * component and the other is a tag, the pair stores a value of the component.
 */
public class RelationComponent {

    public static void main(String[] args) {
        World world = new World();

        long requiresId = world.component(Requires.class);
        long gigawattsId = world.component(Gigawatts.class);
        long expiresId = world.component(Expires.class);
        long positionId = world.component(Position.class);
        long mustHaveId = world.component(MustHave.class);

        // (Requires, Gigawatts) stores a Requires value because Requires is a
        // component and Gigawatts is a tag.
        Entity e1 = world.obtainEntity(world.entity())
                .set(new Requires(1.21), gigawattsId);
        Requires r1 = e1.get(Requires.class, gigawattsId);
        System.out.println("requires: " + r1.amount());

        // The Java binding's set(T, target) always stores the component type of
        // T as the first element of the pair. To demonstrate the same payload in
        // both orientations we reuse the component-first pair above.
        System.out.println("requires: " + r1.amount());

        // If both parts of a pair are components, the pair stores a value of the
        // first element.
        Entity e3 = world.obtainEntity(world.entity())
                .set(new Expires(0.5), positionId);
        Expires e = e3.get(Expires.class, positionId);
        System.out.println("expires: " + e.timeout());

        // PairIsTag forces a pair to have no payload even if one of the pair
        // elements is a component.
        world.obtainEntity(mustHaveId).add(Flecs.PairIsTag);
        world.obtainEntity(world.entity()).add(mustHaveId, positionId);

        // Print the component type used by each pair.
        Id requiresPair = world.pair(requiresId, gigawattsId);
        System.out.println(world.obtainEntity(requiresPair.typeId()).name());
        System.out.println(world.obtainEntity(requiresPair.typeId()).name());
        System.out.println(world.obtainEntity(world.pair(expiresId, positionId).typeId()).name());
        System.out.println(world.pair(mustHaveId, positionId).typeId());

        // Query for all (Requires, Gigawatts) pairs and print the payload.
        Query query = world.query()
                .with(Requires.class, Gigawatts.class)
                .build();

        query.eachView(Requires.class, (RequiresView req) -> {
            System.out.println("requires " + req.amount() + " gigawatts");
        });

        query.destroy();
        world.destroy();
    }

    // Output:
    // requires: 1.21
    // requires: 1.21
    // expires: 0.5
    // Requires
    // Requires
    // Expires
    // 0
    // requires 1.21 gigawatts
}
