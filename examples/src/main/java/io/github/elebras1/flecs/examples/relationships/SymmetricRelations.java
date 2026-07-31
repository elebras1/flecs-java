package io.github.elebras1.flecs.examples.relationships;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.TradesWith;
import io.github.elebras1.flecs.util.Flecs;

/**
 * Demonstrates a symmetric relationship: adding (R, B) to A also adds
 * (R, A) to B. Symmetric relationships are useful for modelling bidirectional
 * links such as alliances or trading partners.
 */
public class SymmetricRelations {

    public static void main(String[] args) {
        World world = new World();

        // Register TradesWith as a symmetric relationship.
        long tradesWithId = world.component(TradesWith.class);
        world.obtainEntity(tradesWithId).add(Flecs.Symmetric);

        // Create two players.
        Entity player1 = world.obtainEntity(world.entity());
        Entity player2 = world.obtainEntity(world.entity());

        // Add (TradesWith, player2) to player1. Because TradesWith is symmetric,
        // (TradesWith, player1) is also added to player2.
        player1.addRelation(tradesWithId, player2.id());

        // Check the relationship in both directions.
        System.out.println("Player 1 trades with Player 2: " + (player1.hasRelation(tradesWithId, player2.id())));
        System.out.println("Player 2 trades with Player 1: " + (player2.hasRelation(tradesWithId, player1.id())));

        world.destroy();
    }

    // Output:
    // Player 1 trades with Player 2: true
    // Player 2 trades with Player 1: true
}
