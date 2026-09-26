package io.github.elebras1.flecs.examples.queries;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Apples;
import io.github.elebras1.flecs.examples.components.Eats;
import io.github.elebras1.flecs.examples.components.Pears;
import io.github.elebras1.flecs.Flecs;

public class Wildcards {

    static void main(String[] args) {
        World world = new World();
        world.component(Eats.class);
        world.component(Apples.class);
        world.component(Pears.class);

        long applesId = world.component(Apples.class);
        long pearsId = world.component(Pears.class);

        // Bob eats 10 apples and 5 pears.
        world.obtainEntity(world.entity("Bob"))
                .set(new Eats(10), applesId)
                .set(new Eats(5), pearsId);

        // Alice eats 4 apples.
        world.obtainEntity(world.entity("Alice"))
                .set(new Eats(4), applesId);

        // Query that matches the (Eats, *) wildcard pair.
        Query query = world.query().with(Eats.class, Flecs.Wildcard).build();

        query.each(Eats.class, (Iter it, int index, Eats eats) -> {
            Entity entity = world.obtainEntity(it.entity(index));
            long foodId = world.obtainId(it.pair(0)).second();
            String foodName = world.obtainEntity(foodId).name();
            System.out.println(entity.name() + " eats " + eats.amount() + " " + foodName);
        });

        query.destroy();
        world.destroy();
    }

    // Output:
    // Alice eats 4 Apples
    // Bob eats 10 Apples
    // Bob eats 5 Pears
}
