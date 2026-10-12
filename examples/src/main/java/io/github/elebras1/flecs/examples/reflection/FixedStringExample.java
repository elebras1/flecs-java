package io.github.elebras1.flecs.examples.reflection;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Nameplate;

public class FixedStringExample {

    static void main(String[] args) {
        try (World world = new World()) {
            world.component(Nameplate.class);

            // title reserves 64 bytes, subtitle uses the default 32 bytes.
            Entity sign = world.obtainEntity(world.entity("Sign"))
                    .set(new Nameplate("Welcome to Flecs", "Press start to play"));

            Nameplate nameplate = sign.get(Nameplate.class);
            System.out.println("title: " + nameplate.title());
            System.out.println("subtitle: " + nameplate.subtitle());

            // Strings longer than length - 1 bytes are truncated.
            sign.set(new Nameplate("This title is too long to fit in a 64 byte buffer, so it gets truncated", "ok"));
            System.out.println("truncated: " + sign.get(Nameplate.class).title());
        }
    }

    // Output:
    // title: Welcome to Flecs
    // subtitle: Press start to play
    // truncated: This title is too long to fit in a 64 byte buffer, so it gets t
}
