package io.github.elebras1.flecs.examples.gamemechanics;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Field;
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
import io.github.elebras1.flecs.util.Flecs;

/**
 * Shows one possible way to implement scene management using pipelines.
 * Entities that belong to the current scene are created as children of a
 * SceneRoot; switching scenes clears those children and swaps the active
 * pipeline.
 */
public class SceneManagement {

    private static void resetScene(World world, long sceneRootId) {
        world.deferBegin();
        world.obtainEntity(sceneRootId).children(id -> world.obtainEntity(id).destruct());
        world.deferEnd();
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
        // other scene-agnostic systems. Use the fully qualified name for the
        // built-in System tag so the query expression can resolve it.
        Pipeline menu = world.pipeline()
                .expr("flecs.system.System, !GameScene")
                .build();
        Pipeline game = world.pipeline()
                .expr("flecs.system.System, !MenuScene")
                .build();

        // Store pipeline ids on the world so observers can retrieve them.
        world.obtainEntity(Flecs.World)
                .set(new MenuScene(menu.id()))
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
        world.system("Print Position")
                .kind(Flecs.OnUpdate)
                .with(Position.class)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        Entity entity = world.obtainEntity(it.entity(i));
                        Position p = positions.get(i);
                        System.out.println(entity.name() + ": {" + p.x() + ", " + p.y() + "}");
                    }
                });

        // Runs only when the game scene is active.
        world.system("Characters Lose Health")
                .kind(gameSceneId)
                .with(Health.class)
                .iter(it -> {
                    Field<Health> healths = it.field(Health.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        Health h = healths.get(i);
                        System.out.println(h.value() + " health remaining");
                        healths.set(i, new Health(h.value() - 1));
                    }
                });

        // Runs only when the menu scene is active.
        world.system("Print Menu Button Text")
                .kind(menuSceneId)
                .with(Button.class)
                .iter(it -> {
                    Field<Button> buttons = it.field(Button.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        System.out.println("Button says \"" + buttons.get(i).text() + "\"");
                    }
                });
    }

    public static void main(String[] args) {
        World world = new World();
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
        world.obtainEntity(Flecs.World).addRelation(activeSceneId, menuSceneId);
        world.progress();

        // Switch to game scene and run a few frames.
        world.obtainEntity(Flecs.World).addRelation(activeSceneId, gameSceneId);
        world.progress();
        world.progress();
        world.progress();

        // Switch back to menu.
        world.obtainEntity(Flecs.World).addRelation(activeSceneId, menuSceneId);
        world.progress();

        // Switch back to game and run a few frames.
        world.obtainEntity(Flecs.World).addRelation(activeSceneId, gameSceneId);
        world.progress();
        world.progress();
        world.progress();

        world.destroy();
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
