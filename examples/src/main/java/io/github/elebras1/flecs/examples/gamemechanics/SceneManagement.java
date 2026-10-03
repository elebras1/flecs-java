package io.github.elebras1.flecs.examples.gamemechanics;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Pipeline;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.ActiveScene;
import io.github.elebras1.flecs.examples.components.Button;
import io.github.elebras1.flecs.examples.components.Character;
import io.github.elebras1.flecs.examples.components.GameScene;
import io.github.elebras1.flecs.examples.components.Health;
import io.github.elebras1.flecs.examples.components.MenuScene;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.SceneRoot;
import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.examples.components.HealthMutView;

/**
 * Shows one possible way to implement scene management using pipelines.
 * Entities that belong to the current scene are created as children of a
 * SceneRoot; switching scenes clears those children and swaps the active
 * pipeline.
 */
public class SceneManagement {

    private static void resetScene(World world, long sceneRootId) {
        world.defer(() -> world.obtainEntity(sceneRootId).children(id -> world.obtainEntity(id).destruct()));
    }

    private static void menuScene(Iter it, long menuSceneId, long sceneRootId) {
        System.out.println("\n>> ActiveScene has changed to `MenuScene`\n");

        World world = it.world();
        resetScene(world, sceneRootId);

        // Create a start menu button when entering the menu scene.
        world.obtainEntity(world.entity("Start Button"))
                .set(new Button("Play the Game!"))
                .set(new Position(50, 50))
                .childOf(sceneRootId);

        long menuPip = world.obtainEntity(Flecs.World).get(MenuScene.class).pip();
        world.setPipeline(menuPip);
    }

    private static void gameScene(Iter it, long gameSceneId, long sceneRootId) {
        System.out.println("\n>> ActiveScene has changed to `GameScene`\n");

        World world = it.world();
        resetScene(world, sceneRootId);

        // Create a player character when entering the game scene.
        world.obtainEntity(world.entity("Player"))
                .set(new Character())
                .set(new Health(2))
                .set(new Position(0, 0))
                .childOf(sceneRootId);

        long gamePip = world.obtainEntity(Flecs.World).get(GameScene.class).pip();
        world.setPipeline(gamePip);
    }

    private static void initScenes(World world, long activeSceneId, long sceneRootId, long menuSceneId, long gameSceneId) {
        // Can only have one active scene at a time.
        world.obtainEntity(activeSceneId).add(Flecs.Exclusive);

        // Each scene gets a pipeline that runs the associated systems plus all
        // other scene-agnostic systems. Use without() for the other scene so
        // that every system without a scene attached to it is included.
        Pipeline menu = world.pipeline()
                .with(Flecs.System)
                .without(GameScene.class)
                .build();
        Pipeline game = world.pipeline()
                .with(Flecs.System)
                .without(MenuScene.class)
                .build();

        // Store pipeline ids on the world so observers can retrieve them.
        world.set(new MenuScene(menu.id()))
                .set(new GameScene(game.id()));

        // Observer for switching to the menu scene.
        world.observer()
                .event(Flecs.OnAdd)
                .with(activeSceneId, menuSceneId)
                .iter(observerIt -> menuScene(observerIt, menuSceneId, sceneRootId));

        // Observer for switching to the game scene.
        world.observer()
                .event(Flecs.OnAdd)
                .with(activeSceneId, gameSceneId)
                .iter(observerIt -> gameScene(observerIt, gameSceneId, sceneRootId));
    }

    private static void initSystems(World world, long menuSceneId, long gameSceneId) {
        // Runs every frame regardless of the current scene.
        world.system("Print Position", Position.class)
                .kind(Flecs.OnUpdate)
                .each(Position.class, (entityId, p) -> {
                    Entity entity = world.obtainEntity(entityId);
                    System.out.println(entity.name() + ": {" + p.x() + ", " + p.y() + "}");
                });

        // Runs only when the game scene is active.
        world.system("Characters Lose Health", Health.class)
                .kind(gameSceneId)
                .eachView(Health.class, (HealthMutView h) -> {
                    System.out.println(h.value() + " health remaining");
                    h.value(h.value() - 1);
                });

        // Runs only when the menu scene is active.
        world.system("Print Menu Button Text", Button.class)
                .kind(menuSceneId)
                .each(Button.class, (Button b) -> System.out.println("Button says \"" + b.text() + "\""));
    }

    public static void main(String[] args) {
        try (World world = new World()) {
            world.component(ActiveScene.class);
            long sceneRootId = world.component(SceneRoot.class);
            long menuSceneId = world.component(MenuScene.class);
            long gameSceneId = world.component(GameScene.class);
            world.component(Character.class);
            world.component(Health.class);
            world.component(Position.class);
            world.component(Button.class);

            long activeSceneId = world.component(ActiveScene.class);
            initScenes(world, activeSceneId, sceneRootId, menuSceneId, gameSceneId);
            initSystems(world, menuSceneId, gameSceneId);

            // Start in the menu scene.
            world.obtainEntity(Flecs.World).add(ActiveScene.class, MenuScene.class);
            world.progress();

            // Switch to game scene and run a few frames.
            world.obtainEntity(Flecs.World).add(ActiveScene.class, GameScene.class);
            world.progress();
            world.progress();
            world.progress();

            // Switch back to menu.
            world.obtainEntity(Flecs.World).add(ActiveScene.class, MenuScene.class);
            world.progress();

            // Switch back to game and run a few frames.
            world.obtainEntity(Flecs.World).add(ActiveScene.class, GameScene.class);
            world.progress();
            world.progress();
            world.progress();

        }
    }

    // Output:
    //
    // >> ActiveScene has changed to `MenuScene`
    //
    // Start Button: {50.0, 50.0}
    // Button says "Play the Game!"
    //
    // >> ActiveScene has changed to `GameScene`
    //
    // Player: {0.0, 0.0}
    // 2 health remaining
    // Player: {0.0, 0.0}
    // 1 health remaining
    // Player: {0.0, 0.0}
    // 0 health remaining
    //
    // >> ActiveScene has changed to `MenuScene`
    //
    // Start Button: {50.0, 50.0}
    // Button says "Play the Game!"
    //
    // >> ActiveScene has changed to `GameScene`
    //
    // Player: {0.0, 0.0}
    // 2 health remaining
    // Player: {0.0, 0.0}
    // 1 health remaining
    // Player: {0.0, 0.0}
    // 0 health remaining
}
