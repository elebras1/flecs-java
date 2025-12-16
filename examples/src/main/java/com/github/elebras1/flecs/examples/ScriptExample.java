package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.*;

public class ScriptExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            String flecsScript = """
                using flecs.meta

                struct "com.github.elebras1.flecs.examples.components.Position" {
                    x = f32
                    y = f32
                }
            
                struct "com.github.elebras1.flecs.examples.components.Label" {
                    label = string
                }
            
                struct "com.github.elebras1.flecs.examples.components.Ideology" {
                    color = i32
                    factionDriftingSpeed = i8
                    stabilityIndex = i16
                }
            
                using com.github.elebras1.flecs.examples.components
            
                Dictator {
                    Position: { x: 100.5, y: -50.0 }
                    Label: { label: "Supreme Leader" }
                    Ideology: { 
                        color: 16711680, 
                        factionDriftingSpeed: 10, 
                        stabilityIndex: 50 
                    }
                }
            """;

            world.script(flecsScript).name("LevelInit").run();

            // Register components after script execution
            world.component(Position.class);
            world.component(Ideology.class);
            world.component(Label.class);

            long entityId = world.lookup("Dictator");

            Entity entity = world.obtainEntity(entityId);

            Position position = entity.get(Position.class);
            Label label = entity.get(Label.class);
            Ideology ideology = entity.get(Ideology.class);

            System.out.println("Entity Label : " + label.label());
            System.out.printf("Position : %.2f, %.2f%n", position.x(), position.y());
            System.out.printf("Ideology : Color: %d, Drifting Speed: %d, Stability Index: %d%n", ideology.color(), ideology.factionDriftingSpeed(), ideology.stabilityIndex());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}