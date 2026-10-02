package io.github.elebras1.flecs.examples.relationships;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.TradesWith;
import io.github.elebras1.flecs.Flecs;

/**
 * Demonstrates a symmetric relationship: adding (R, B) to A also adds
 * (R, A) to B. Symmetric relationships are useful for modelling bidirectional
 * links such as alliances or trading partners.
 */
public class SymmetricRelations {

    public static void main(String[] args) {
        try (World world = new World()) {

            // Register TradesWith as a symmetric relationship.
            world.obtainEntity(world.component(TradesWith.class)).add(Flecs.Symmetric);

            // Create two players.
            Entity player1 = world.obtainEntity(world.entity());
            Entity player2 = world.obtainEntity(world.entity());

            // Add (TradesWith, player2) to player1. Because TradesWith is symmetric,
            // (TradesWith, player1) is also added to player2.
            player1.add(TradesWith.class, player2);

            // Check the relationship in both directions.
            System.out.println("Player 1 trades with Player 2: " + (player1.has(TradesWith.class, player2)));
            System.out.println("Player 2 trades with Player 1: " + (player2.has(TradesWith.class, player1)));

        }
    }

    // Output:
    // Player 1 trades with Player 2: true
    // Player 2 trades with Player 1: true
}
