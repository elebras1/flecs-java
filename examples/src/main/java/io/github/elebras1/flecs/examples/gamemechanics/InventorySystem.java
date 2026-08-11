package io.github.elebras1.flecs.examples.gamemechanics;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Active;
import io.github.elebras1.flecs.examples.components.Amount;
import io.github.elebras1.flecs.examples.components.Armor;
import io.github.elebras1.flecs.examples.components.Attack;
import io.github.elebras1.flecs.examples.components.Coin;
import io.github.elebras1.flecs.examples.components.Container;
import io.github.elebras1.flecs.examples.components.ContainedBy;
import io.github.elebras1.flecs.examples.components.Health;
import io.github.elebras1.flecs.examples.components.Inventory;
import io.github.elebras1.flecs.examples.components.Item;
import io.github.elebras1.flecs.examples.components.Sword;
import io.github.elebras1.flecs.util.Flecs;

/**
 * Demonstrates one possible way to implement an inventory system using ECS
 * relationships. Items can be contained by containers, equipped as active,
 * and used to attack with armor and health mechanics.
 */
public class InventorySystem {

    private static long itemKindId(World world, long entityId, long swordId, long armorId, long coinId) {
        Entity entity = world.obtainEntity(entityId);
        if (entity.has(Sword.class)) {
            return swordId;
        }
        if (entity.has(Armor.class)) {
            return armorId;
        }
        if (entity.has(Coin.class)) {
            return coinId;
        }
        return 0;
    }

    private static String itemName(World world, long entityId) {
        Entity entity = world.obtainEntity(entityId);
        long prefab = entity.target(Flecs.IsA, 0);
        if (prefab != 0) {
            String name = world.obtainEntity(prefab).name();
            if (name != null) return name;
        }
        if (entity.has(Sword.class)) {
            return "Sword";
        }
        if (entity.has(Armor.class)) {
            return "Armor";
        }
        if (entity.has(Coin.class)) {
            return "Coin";
        }
        return "Item";
    }

    private static String kindName(long kindId, long swordId, long armorId, long coinId) {
        if (kindId == swordId) {
            return "Sword";
        }
        if (kindId == armorId) {
            return "Armor";
        }
        if (kindId == coinId) {
            return "Coin";
        }
        return "Item";
    }

    private static Entity getContainer(World world, long inventoryId, Entity entity) {
        if (entity.has(Container.class)) {
            return entity;
        }
        long inventory = entity.target(inventoryId, 0);
        return inventory != 0 ? world.obtainEntity(inventory) : entity;
    }

    private static long findItemWithKind(World world, long containerId, long kindId, boolean activeRequired, long swordId, long armorId, long coinId) {
        Query q = world.query().with(ContainedBy.class, containerId).build();
        long[] result = { 0 };
        q.each(itemId -> {
            if (result[0] != 0) return;
            Entity item = world.obtainEntity(itemId);
            if (activeRequired && !item.has(Active.class)) {
                return;
            }
            if (itemKindId(world, itemId, swordId, armorId, coinId) == kindId) {
                result[0] = itemId;
            }
        });
        q.destroy();
        return result[0];
    }

    private static void transferItem(World world, long containedById, Entity container, Entity item, long swordId, long armorId, long coinId) {
        Amount amount = item.get(Amount.class);
        if (amount != null) {
            long kindId = itemKindId(world, item.id(), swordId, armorId, coinId);
            long existingId = findItemWithKind(world, container.id(), kindId, false, swordId, armorId, coinId);
            if (existingId != 0) {
                Entity existing = world.obtainEntity(existingId);
                Amount existingAmount = existing.get(Amount.class);
                int newValue = existingAmount != null ? existingAmount.value() + amount.value() : amount.value();
                existing.set(new Amount(newValue));
                item.destruct();
                return;
            }
        }
        item.add(containedById, container.id());
    }

    private static void transferItems(World world, long containedById, long inventoryId, Entity dst, Entity src, long swordId, long armorId, long coinId) {
        System.out.println(">> Transfer items from " + src.name() + " to " + dst.name() + "\n");

        world.deferBegin();
        Entity dstContainer = getContainer(world, inventoryId, dst);
        Entity srcContainer = getContainer(world, inventoryId, src);

        Query q = world.query().with(ContainedBy.class, srcContainer.id()).build();
        q.each(itemId -> {
            Entity item = world.obtainEntity(itemId);
            transferItem(world, containedById, dstContainer, item, swordId, armorId, coinId);
        });
        q.destroy();
        world.deferEnd();
    }

    private static void attack(World world, long inventoryId, Entity player, Entity weapon, long swordId, long armorId) {
        System.out.println(">> " + player.name() + " is attacked with a " + itemName(world, weapon.id()) + "!");

        Attack attack = weapon.get(Attack.class);
        if (attack == null) {
            System.out.println(" - the weapon is a dud");
            return;
        }

        int attackValue = (int) attack.value();

        Entity playerContainer = getContainer(world, inventoryId, player);
        long armorIdFound = findItemWithKind(world, playerContainer.id(), armorId, true, swordId, armorId, 0);
        if (armorIdFound != 0) {
            Entity armor = world.obtainEntity(armorIdFound);
            Health armorHealth = armor.get(Health.class);
            if (armorHealth == null) {
                System.out.println(" - the " + itemName(world, armor.id()) + " armor is a dud");
            } else {
                System.out.println(" - " + player.name() + " defends with " + itemName(world, armor.id()));

                int remaining = armorHealth.value() - attackValue;
                if (remaining <= 0) {
                    attackValue = -remaining;
                    System.out.println(" - " + itemName(world, armor.id()) + " is destroyed!");
                    armor.destruct();
                } else {
                    armor.set(new Health(remaining));
                    System.out.println(" - " + itemName(world, armor.id()) + " has " + remaining + " health left after taking " + attackValue + " damage");
                    attackValue = 0;
                }
            }
        } else {
            System.out.println(" - " + player.name() + " fights without armor!");
        }

        Health weaponHealth = weapon.get(Health.class);
        if (weaponHealth != null) {
            int remaining = weaponHealth.value() - 1;
            if (remaining <= 0) {
                System.out.println(" - " + itemName(world, weapon.id()) + " is destroyed!");
                weapon.destruct();
            } else {
                weapon.set(new Health(remaining));
                System.out.println(" - " + itemName(world, weapon.id()) + " has " + remaining + " uses left");
            }
        }

        if (attackValue > 0) {
            Health playerHealth = player.get(Health.class);
            if (playerHealth != null) {
                int remaining = playerHealth.value() - attackValue;
                if (remaining <= 0) {
                    System.out.println(" - " + player.name() + " died!");
                    player.destruct();
                } else {
                    player.set(new Health(remaining));
                    System.out.println(" - " + player.name() + " has " + remaining + " health left after taking " + attackValue + " damage");
                }
            }
        }

        System.out.println();
    }

    private static void printItems(World world, long inventoryId,
                                   long swordId, long armorId, long coinId, Entity container) {
        System.out.println("-- " + container.name() + "'s inventory:");
        Entity actualContainer = getContainer(world, inventoryId, container);

        int[] count = { 0 };
        Query q = world.query().with(ContainedBy.class, actualContainer.id()).build();
        q.each(itemId -> {
            Entity item = world.obtainEntity(itemId);
            Amount amount = item.get(Amount.class);
            int amountValue = amount != null ? amount.value() : 1;

            String name = itemName(world, itemId);
            String kind = kindName(itemKindId(world, itemId, swordId, armorId, coinId), swordId, armorId, coinId);

            System.out.println(" - " + amountValue + " " + name + (amountValue > 1 ? "s" : "") + " (" + kind + ")");
            count[0]++;
        });
        q.destroy();

        if (count[0] == 0) {
            System.out.println(" - << empty >>");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        World world = new World();
        long containedById = world.component(ContainedBy.class);
        long inventoryId = world.component(Inventory.class);
        long containerId = world.component(Container.class);
        long itemId = world.component(Item.class);
        long swordId = world.component(Sword.class);
        long armorId = world.component(Armor.class);
        long coinId = world.component(Coin.class);
        world.component(Amount.class);
        world.component(Attack.class);
        world.component(Health.class);
        world.component(Active.class);

        // ContainedBy is exclusive: an item can only be in one container.
        world.obtainEntity(containedById).add(Flecs.Exclusive);

        // Item kinds inherit from Item.
        world.obtainEntity(swordId).add(Flecs.IsA, itemId);
        world.obtainEntity(armorId).add(Flecs.IsA, itemId);
        world.obtainEntity(coinId).add(Flecs.IsA, itemId);

        // Register prefabs.
        long woodenSword = world.prefab();
        world.obtainEntity(woodenSword).name("WoodenSword")
                .add(Sword.class)
                .set(new Attack(1))
                .set(new Health(5))
                .autoOverride(Health.class);

        long ironSword = world.prefab();
        world.obtainEntity(ironSword).name("IronSword")
                .add(Sword.class)
                .set(new Attack(2))
                .set(new Health(10))
                .autoOverride(Health.class);

        long woodenArmor = world.prefab();
        world.obtainEntity(woodenArmor).name("WoodenArmor")
                .add(Armor.class)
                .set(new Health(10))
                .autoOverride(Health.class);

        long ironArmor = world.prefab();
        world.obtainEntity(ironArmor).name("IronArmor")
                .add(Armor.class)
                .set(new Health(20))
                .autoOverride(Health.class);

        // Create a loot box with items.
        Entity chest = world.obtainEntity(world.entity("Chest")).add(Container.class);
        world.obtainEntity(world.entity()).isA(ironSword).add(containedById, chest.id());
        world.obtainEntity(world.entity()).isA(woodenArmor).add(containedById, chest.id());
        world.obtainEntity(world.entity()).add(Coin.class).set(new Amount(30))
                .add(containedById, chest.id());

        // Create a player with an inventory containing some coins.
        Entity playerInventory = world.obtainEntity(world.entity()).add(Container.class);
        world.obtainEntity(world.entity()).add(Coin.class).set(new Amount(20))
                .add(containedById, playerInventory.id());

        Entity player = world.obtainEntity(world.entity("Player"))
                .set(new Health(10))
                .add(inventoryId, playerInventory.id());

        // Print initial inventories.
        printItems(world, inventoryId, swordId, armorId, coinId, chest);
        printItems(world, inventoryId, swordId, armorId, coinId, player);

        // Move items from chest to player.
        transferItems(world, containedById, inventoryId, player, chest,
                swordId, armorId, coinId);

        printItems(world, inventoryId, swordId, armorId, coinId, player);
        printItems(world, inventoryId, swordId, armorId, coinId, chest);

        // Equip armor if available.
        Entity playerContainer = getContainer(world, inventoryId, player);
        long foundArmor = findItemWithKind(world, playerContainer.id(),
                armorId, false, swordId, armorId, coinId);
        if (foundArmor != 0) {
            world.obtainEntity(foundArmor).add(Active.class);
        }

        // Create a weapon and attack the player.
        Entity mySword = world.obtainEntity(world.entity()).isA(ironSword);
        attack(world, inventoryId, player, mySword, swordId, armorId);

        world.destroy();
    }

    // Output:
    // -- Chest's inventory:
    //  - 1 IronSword (Sword)
    //  - 1 WoodenArmor (Armor)
    //  - 30 Coins (Coin)
    //
    // -- Player's inventory:
    //  - 20 Coins (Coin)
    //
    // >> Transfer items from Chest to Player
    //
    // -- Player's inventory:
    //  - 50 Coins (Coin)
    //  - 1 IronSword (Sword)
    //  - 1 WoodenArmor (Armor)
    //
    // -- Chest's inventory:
    //  - << empty >>
    //
    // >> Player is attacked with a IronSword!
    //  - Player defends with WoodenArmor
    //  - WoodenArmor has 8 health left after taking 2 damage
    //  - IronSword has 9 uses left
}
