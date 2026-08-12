package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Position;
import io.github.elebras1.flecs.component.Velocity;
import io.github.elebras1.flecs.util.Flecs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleTest {

    private World world;

    @BeforeEach
    void init() {
        this.world = new World();
    }

    @AfterEach
    void tearDown() {
        this.world.destroy();
    }

    @Test
    void importModule() {
        Entity module = this.world.importModule(new SimpleModule());
        assertNotNull(module);
        assertTrue(module.id() != 0);
        assertTrue(module.has(Flecs.Module));

        long position = module.lookup("Position");
        assertTrue(position != 0);
        assertEquals(0, this.world.getScope());
    }

    @Test
    void lookupFromScope() {
        Entity module = this.world.importModule(new SimpleModule());
        long moduleEntity = module.id();
        assertTrue(moduleEntity != 0);

        long positionEntity = module.lookup("Position");
        assertTrue(positionEntity != 0);
        assertEquals(positionEntity, module.lookup("Position"));
        assertEquals(0, this.world.getScope());
    }

    @Test
    void componentRedefinitionOutsideModule() {
        Entity module = this.world.importModule(new SimpleModule());

        long modulePosition = module.lookup("Position");
        assertTrue(modulePosition != 0);

        long position = this.world.component(Position.class);
        assertTrue(position != 0);
        assertEquals(modulePosition, position);
    }

    @Test
    void importTwice() {
        Entity module1 = this.world.importModule(new SimpleModule());
        Entity module2 = this.world.importModule(new SimpleModule());
        assertEquals(module1.id(), module2.id());
    }

    @Test
    void moduleSystemRuns() {
        this.world.importModule(new SimpleModule());

        Entity entity = this.world.obtainEntity(this.world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 2));

        this.world.progress(0.0f);

        Position p = entity.get(Position.class);
        assertNotNull(p);
        assertEquals(11.0f, p.x());
        assertEquals(22.0f, p.y());
    }

    public static class SimpleModule implements FlecsModule {
        @Override
        public void initModule(World world) {
            world.module(this);

            world.component(Position.class);
            world.component(Velocity.class);

            world.system("Move")
                    .with(Position.class)
                    .with(Velocity.class)
                    .kind(Flecs.OnUpdate)
                    .iter(it -> {
                        Field<Position> positions = it.field(Position.class, 0);
                        Field<Velocity> velocities = it.field(Velocity.class, 1);
                        for (int i = 0; i < it.count(); i++) {
                            Position p = positions.get(i);
                            Velocity v = velocities.get(i);
                            positions.set(i, new Position(p.x() + v.x(), p.y() + v.y()));
                        }
                    });
        }
    }
}
