package io.github.elebras1.flecs;

import io.github.elebras1.flecs.component.Position;
import io.github.elebras1.flecs.component.Velocity;
import io.github.elebras1.flecs.callback.ComparatorId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class ObserverTest {

    private World world;

    @BeforeEach
    void init() {
        this.world = new World();
        this.world.component(Position.class);
        this.world.component(Velocity.class);
    }

    @AfterEach
    void tearDown() {
        this.world.destroy();
    }

    @Test
    void onAdd() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .each(entityId -> count.incrementAndGet());

        this.world.obtainEntity(this.world.entity()).set(new Position(10, 20));
        assertEquals(1, count.get());
    }

    @Test
    void onRemove() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer(Position.class)
                .event(Flecs.OnRemove)
                .each(entityId -> count.incrementAndGet());

        Entity e = this.world.obtainEntity(this.world.entity()).add(Position.class);
        assertEquals(0, count.get());

        e.remove(Position.class);
        assertEquals(1, count.get());

        e.remove(Position.class);
        assertEquals(1, count.get());
    }

    @Test
    void onSet() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnSet)
                .with(Position.class)
                .each(entityId -> count.incrementAndGet());

        Entity e = this.world.obtainEntity(this.world.entity());
        e.set(new Position(10, 20));
        assertEquals(1, count.get());

        e.set(new Position(30, 40));
        assertEquals(2, count.get());
    }

    @Test
    void twoTermsOnAdd() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .with(Velocity.class)
                .each(entityId -> count.incrementAndGet());

        Entity e = this.world.obtainEntity(this.world.entity());
        assertEquals(0, count.get());

        e.set(new Position(10, 20));
        assertEquals(0, count.get());

        e.set(new Velocity(1, 2));
        assertEquals(1, count.get());
    }

    @Test
    void twoTermsOnSet() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnSet)
                .with(Position.class)
                .with(Velocity.class)
                .each(entityId -> count.incrementAndGet());

        Entity e = this.world.obtainEntity(this.world.entity());
        e.set(new Position(10, 20));
        assertEquals(0, count.get());

        e.set(new Velocity(1, 2));
        assertEquals(1, count.get());
    }

    @Test
    void twoEntitiesEach() {
        long e1 = this.world.entity();
        long e2 = this.world.entity();

        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnSet)
                .with(Position.class)
                .each(entityId -> {
                    if (entityId == e1 || entityId == e2) {
                        count.incrementAndGet();
                    }
                });

        this.world.obtainEntity(e1).set(new Position(10, 20));
        assertEquals(1, count.get());

        this.world.obtainEntity(e2).set(new Position(30, 40));
        assertEquals(2, count.get());
    }

    @Test
    void twoEntitiesIter() {
        long e1 = this.world.entity();
        long e2 = this.world.entity();

        AtomicInteger count = new AtomicInteger();
        Map<Long, Float> xs = new HashMap<>();
        this.world.observer()
                .event(Flecs.OnSet)
                .with(Position.class)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        xs.put(it.entity(i), positions.get(i).x());
                        count.incrementAndGet();
                    }
                });

        this.world.obtainEntity(e1).set(new Position(10, 20));
        assertEquals(1, count.get());

        this.world.obtainEntity(e2).set(new Position(30, 40));
        assertEquals(2, count.get());
        assertEquals(10.0f, xs.get(e1));
        assertEquals(30.0f, xs.get(e2));
    }

    @Test
    void createWithoutTypeArgs() {
        long e1 = this.world.entity();

        AtomicInteger count = new AtomicInteger();
        List<Long> ids = new ArrayList<>();
        this.world.observer()
                .with(Position.class)
                .event(Flecs.OnAdd)
                .each((long entityId) -> ids.add(entityId));

        this.world.obtainEntity(e1).set(new Position(10, 20));
        assertEquals(List.of(e1), ids);
        assertEquals(1, ids.size());
    }

    @Test
    void eachWithIterNoComponents() {
        long e1 = this.world.entity();

        List<Long> ids = new ArrayList<>();
        this.world.observer()
                .with(Position.class)
                .event(Flecs.OnAdd)
                .each((Iter it, int index) -> ids.add(it.entity(index)));

        this.world.obtainEntity(e1).set(new Position(10, 20));
        assertEquals(List.of(e1), ids);
    }

    @Test
    void onAddTag() {
        long tag = this.world.entity();

        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(tag)
                .each(entityId -> count.incrementAndGet());

        this.world.obtainEntity(this.world.entity()).add(tag);
        assertEquals(1, count.get());
    }

    @Test
    void yieldExisting() {

        long e1 = this.world.obtainEntity(this.world.entity()).add(Position.class).id();

        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .yieldExisting()
                .each(entityId -> count.incrementAndGet());

        assertEquals(1, count.get());

        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        assertEquals(2, count.get());
    }

    @Test
    void observerFlagsAccumulate() {
        long e1 = this.world.obtainEntity(this.world.entity()).add(Position.class).id();

        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .observerFlags(Flecs.ObserverYieldOnCreate)
                .observerFlags(Flecs.ObserverYieldOnDelete)
                .each(entityId -> count.incrementAndGet());

        assertEquals(1, count.get());
    }

    @Test
    void observerMatchPrefab() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .observerFlags(Flecs.ObserverMatchPrefab)
                .each(entityId -> count.incrementAndGet());

        Entity prefab = this.world.obtainEntity(this.world.entity())
                .add(Flecs.Prefab)
                .add(Position.class);
        assertEquals(1, count.get());
    }

    @Test
    void observerMatchDisabled() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .observerFlags(Flecs.ObserverMatchDisabled)
                .each(entityId -> count.incrementAndGet());

        Entity disabled = this.world.obtainEntity(this.world.entity())
                .add(Flecs.Disabled)
                .add(Position.class);
        assertEquals(1, count.get());
    }

    @Test
    void observerNotMatchPrefabByDefault() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .each(entityId -> count.incrementAndGet());

        Entity prefab = this.world.obtainEntity(this.world.entity())
                .add(Flecs.Prefab)
                .add(Position.class);
        this.world.obtainEntity(this.world.entity()).add(Position.class);
        assertEquals(1, count.get());
    }

    @Test
    void observerQueryFlags() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .queryFlags(Flecs.QueryMatchEmptyTables)
                .cached()
                .each(entityId -> count.incrementAndGet());

        this.world.obtainEntity(this.world.entity()).add(Position.class);
        assertEquals(1, count.get());
    }

    @Test
    void observerFlagsRejectsYieldWithYieldExisting() {
        assertThrows(IllegalStateException.class, () -> this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .yieldExisting()
                .observerFlags(Flecs.ObserverYieldOnCreate));

        assertThrows(IllegalStateException.class, () -> this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .observerFlags(Flecs.ObserverYieldOnDelete)
                .yieldExisting());
    }

    @Test
    void onAddExpr() {
        long tag = this.world.entity();

        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(tag)
                .each(entityId -> count.incrementAndGet());

        Entity e = this.world.obtainEntity(this.world.entity()).add(tag);
        assertEquals(1, count.get());

        e.remove(tag);
        assertEquals(1, count.get());
    }

    @Test
    void runCallback() {
        AtomicInteger count = new AtomicInteger();
        this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .iter(it -> {
                    for (int i = 0; i < it.count(); i++) {
                        count.incrementAndGet();
                    }
                });

        Entity e = this.world.obtainEntity(this.world.entity());
        assertEquals(0, count.get());

        e.set(new Position(10, 20));
        assertEquals(1, count.get());
    }

    @Test
    void onSetWithSet() {
        AtomicInteger count = new AtomicInteger();
        List<Float> xs = new ArrayList<>();
        this.world.observer()
                .event(Flecs.OnSet)
                .with(Position.class)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        xs.add(positions.get(i).x());
                        count.incrementAndGet();
                    }
                });

        Entity e = this.world.obtainEntity(this.world.entity());
        e.set(new Position(10, 20));
        assertEquals(1, count.get());
        assertEquals(List.of(10.0f), xs);
    }

    @Test
    void enableDisable() {
        AtomicInteger count = new AtomicInteger();
        Observer observer = this.world.observer()
                .event(Flecs.OnAdd)
                .with(Position.class)
                .each(entityId -> count.incrementAndGet());

        observer.disable();
        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        assertEquals(0, count.get());

        observer.enable();
        this.world.obtainEntity(this.world.entity()).set(new Position(3, 4));
        assertEquals(1, count.get());
    }

    @Test
    void parentTerm() {
        Entity sun = this.world.obtainEntity(this.world.entity("Sun")).set(new Position(10, 20));
        Entity earth = this.world.obtainEntity(this.world.entity("Earth")).childOf(sun);

        AtomicInteger count = new AtomicInteger();
        this.world.observer("ParentObserver")
                .event(Flecs.OnSet)
                .with(Position.class)
                .with(Position.class).parent()
                .each(entityId -> count.incrementAndGet());

        earth.set(new Position(5, 6));
        assertEquals(1, count.get());
    }

    @Test
    void observerCtx() {
        Object ctx = new Object();
        Observer observer = this.world.observer("CtxObserver")
                .event(Flecs.OnAdd)
                .with(Position.class)
                .ctx(ctx)
                .each(entityId -> { });

        assertEquals(ctx, observer.getCtx());

        Object ctx2 = new Object();
        observer.setCtx(ctx2);
        assertEquals(ctx2, observer.getCtx());
    }

    @Test
    void idFlags() {
        long positionId = this.world.component(Position.class);
        this.world.obtainEntity(positionId).add(Flecs.CanToggle);

        Observer observer = this.world.observer()
                .with(Position.class).idFlags(Flecs.Toggle)
                .event(Flecs.OnSet)
                .each(entityId -> { });

        assertNotEquals(0, observer.id());
    }

    @Test
    void filterTerm() {
        Observer observer = this.world.observer(Position.class)
                .with(Velocity.class).filter()
                .event(Flecs.OnAdd)
                .each(Position.class, (Iter it, int index, Position p) -> { });

        assertNotEquals(0, observer.id());
    }

    @Test
    void iterEvent() {
        AtomicLong event = new AtomicLong();
        this.world.observer(Position.class)
                .event(Flecs.OnAdd)
                .each(Position.class, (Iter it, int index, Position p) -> event.set(it.event()));

        this.world.obtainEntity(this.world.entity()).add(Position.class);

        assertEquals(Flecs.OnAdd, event.get());
    }

    @Test
    void termIdAndPairOverloads() {
        long tag = this.world.entity("ObserverTag");
        long rel = this.world.entity("ObserverRel");
        long target = this.world.entity("ObserverTarget");

        AtomicInteger count = new AtomicInteger();

        this.world.observer("TagTermObserver")
                .event(Flecs.OnAdd)
                .with(tag)
                .each(entityId -> count.addAndGet(1));
        this.world.observer("EntityTermObserver")
                .event(Flecs.OnAdd)
                .with(this.world.obtainEntity(tag))
                .each(entityId -> count.addAndGet(2));
        this.world.observer("NameTermObserver")
                .event(Flecs.OnAdd)
                .with("ObserverTag")
                .each(entityId -> count.addAndGet(4));
        this.world.observer("PairTermObserver")
                .event(Flecs.OnAdd)
                .with(rel, target)
                .each(entityId -> count.addAndGet(8));
        this.world.observer("NamePairTermObserver")
                .event(Flecs.OnAdd)
                .with("ObserverRel", "ObserverTarget")
                .each(entityId -> count.addAndGet(16));

        this.world.obtainEntity(this.world.entity()).add(tag);
        this.world.obtainEntity(this.world.entity()).add(rel, target);

        assertEquals(31, count.get());
    }

    @Test
    void termClassPairOverloads() {
        long target = this.world.entity("ObserverClassTarget");

        AtomicInteger count = new AtomicInteger();

        this.world.observer("ClassTargetTermObserver")
                .event(Flecs.OnAdd)
                .with(Position.class, target)
                .each(entityId -> count.addAndGet(1));
        this.world.observer("ClassEntityTermObserver")
                .event(Flecs.OnAdd)
                .with(Velocity.class, this.world.obtainEntity(target))
                .each(entityId -> count.addAndGet(2));
        this.world.observer("NameTargetTermObserver")
                .event(Flecs.OnAdd)
                .with("ObserverClassTarget", target)
                .each(entityId -> count.addAndGet(4));
        this.world.observer("ClassClassTermObserver")
                .event(Flecs.OnAdd)
                .with(Position.class, Velocity.class)
                .each(entityId -> count.addAndGet(8));

        Entity entity = this.world.obtainEntity(this.world.entity());
        entity.add(Position.class, target);
        entity.add(Velocity.class, target);
        entity.add(target, target);
        entity.add(this.world.id(Position.class), this.world.id(Velocity.class));

        assertEquals(15, count.get());
    }

    @Test
    void termWithoutOverloads() {
        long tag = this.world.entity("ObserverSkipTag");
        Entity skipEntity = this.world.obtainEntity(this.world.entity());
        this.world.obtainEntity(this.world.entity("ObserverSkipName"));
        long rel = this.world.entity("ObserverSkipRel");
        long target = this.world.entity("ObserverSkipTarget");

        AtomicInteger count = new AtomicInteger();

        this.world.observer("WithoutTermObserver")
                .event(Flecs.OnAdd)
                .with(Position.class)
                .without(tag)
                .without(skipEntity)
                .without(Velocity.class)
                .without("ObserverSkipName")
                .without(rel, target)
                .without("ObserverSkipRel", "ObserverSkipTarget")
                .without(Velocity.class, target)
                .without(Velocity.class, skipEntity)
                .without(Velocity.class, "ObserverSkipName")
                .without(Position.class, Velocity.class)
                .each(entityId -> count.incrementAndGet());

        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        assertEquals(1, count.get());

        Entity skipped = this.world.obtainEntity(this.world.entity());
        skipped.add(tag);
        skipped.set(new Position(1, 2));
        assertEquals(1, count.get());
    }

    @Test
    void termModifiers() {
        long skip = this.world.entity("ObserverModifierSkip");

        AtomicInteger count = new AtomicInteger();

        this.world.observer("ModifierTermObserver")
                .event(Flecs.OnAdd)
                .with(Position.class).self().in()
                .with(Velocity.class).termAt(1).out()
                .without(skip)
                .each(entityId -> count.incrementAndGet());

        this.world.obtainEntity(this.world.entity()).set(new Position(1, 2));
        assertEquals(0, count.get());

        Entity entity = this.world.obtainEntity(this.world.entity());
        entity.set(new Position(1, 2));
        entity.set(new Velocity(1, 2));
        assertEquals(1, count.get());

        Entity excluded = this.world.obtainEntity(this.world.entity());
        excluded.add(skip);
        excluded.set(new Position(1, 2));
        excluded.set(new Velocity(1, 2));
        assertEquals(1, count.get());

        Observer aliases = this.world.observer("ModifierAliasObserver")
                .event(Flecs.OnAdd)
                .with(Position.class).read()
                .with(Velocity.class).write().readWrite().inout().inout(Flecs.InOutFilter).filter()
                .each(entityId -> { });
        assertNotEquals(0, aliases.id());

        Observer operators = this.world.observer("OperatorObserver")
                .event(Flecs.OnAdd)
                .with(Position.class).and()
                .with(Velocity.class).optional()
                .each(entityId -> { });
        assertNotEquals(0, operators.id());
    }

    @Test
    void termSelectionAndSource() {
        long rel = this.world.entity("ObserverSecondRel");
        long target = this.world.entity("ObserverSecondTarget");
        long source = this.world.entity("ObserverSource");

        AtomicInteger count = new AtomicInteger();

        this.world.observer("SecondTermObserver")
                .event(Flecs.OnAdd)
                .with(rel).second(target)
                .each(entityId -> count.incrementAndGet());

        this.world.obtainEntity(this.world.entity()).add(rel, target);
        assertEquals(1, count.get());

        Observer secondByName = this.world.observer("SecondTermByNameObserver")
                .event(Flecs.OnAdd)
                .with(rel).second("ObserverSecondTarget")
                .each(entityId -> { });
        assertNotEquals(0, secondByName.id());

        Observer selected = this.world.observer("SelectedTermObserver")
                .event(Flecs.OnAdd)
                .with(Position.class)
                .with(Velocity.class)
                .in()
                .termAt(0).inout()
                .each(entityId -> { });
        assertNotEquals(0, selected.id());

        Observer sourced = this.world.observer("SourceTermObserver")
                .event(Flecs.OnAdd)
                .with(Position.class).src(source)
                .each(entityId -> { });
        assertNotEquals(0, sourced.id());

        Observer sourcedByName = this.world.observer("SourceTermByNameObserver")
                .event(Flecs.OnAdd)
                .with(Position.class).src("ObserverSource")
                .each(entityId -> { });
        assertNotEquals(0, sourcedByName.id());

        Observer flagged = this.world.observer("FlaggedTermObserver")
                .event(Flecs.OnAdd)
                .with(Flecs.PredEq).second("Position").flags(Flecs.IsName)
                .each(entityId -> { });
        assertNotEquals(0, flagged.id());
    }

    @Test
    void termTraversalModifiers() {
        Entity sun = this.world.obtainEntity(this.world.entity("ObserverSun")).set(new Velocity(1, 2));
        Entity earth = this.world.obtainEntity(this.world.entity("ObserverEarth")).childOf(sun);

        AtomicInteger count = new AtomicInteger();

        this.world.observer("TraversalTermObserver")
                .event(Flecs.OnSet)
                .with(Velocity.class)
                .with(Velocity.class).up().trav(Flecs.ChildOf)
                .each(entityId -> count.incrementAndGet());

        earth.set(new Velocity(3, 4));
        assertEquals(1, count.get());

        Observer variable = this.world.observer("VariableTermObserver")
                .event(Flecs.OnAdd)
                .with(Position.class).var("ObserverVariable")
                .each(entityId -> { });
        assertNotEquals(0, variable.id());
    }

    @Test
    void termOperators() {
        long prefab = this.world.prefab("ObserverPrefab");
        this.world.obtainEntity(prefab).set(new Position(1, 2));

        Observer andFrom = this.world.observer("AndFromTermObserver")
                .event(Flecs.OnAdd)
                .with(prefab).andFrom()
                .each(entityId -> { });
        assertNotEquals(0, andFrom.id());

        Observer orFrom = this.world.observer("OrFromTermObserver")
                .event(Flecs.OnAdd)
                .with(prefab).orFrom()
                .each(entityId -> { });
        assertNotEquals(0, orFrom.id());

        Observer notFrom = this.world.observer("NotFromTermObserver")
                .event(Flecs.OnAdd)
                .with(prefab).notFrom()
                .each(entityId -> { });
        assertNotEquals(0, notFrom.id());
    }

    @Test
    void termQueryOptions() {
        Observer expr = this.world.observer("ExprTermObserver")
                .event(Flecs.OnAdd)
                .expr("Position")
                .each(entityId -> { });
        assertNotEquals(0, expr.id());

        Observer options = this.world.observer("OptionsTermObserver")
                .event(Flecs.OnAdd)
                .with(Position.class)
                .cached()
                .queryFlags(Flecs.QueryMatchEmptyTables)
                .each(entityId -> { });
        assertNotEquals(0, options.id());
    }

    @Test
    void termFeaturesUnsupportedByObserverQueries() {
        long region = this.world.entity("ObserverRegion");

        assertThrows(IllegalStateException.class, () -> this.world.observer("OrderByTermObserver")
                .event(Flecs.OnAdd)
                .with(Position.class)
                .orderBy(Position.class, (ComparatorId) (a, b) -> Long.compare(a, b))
                .each(entityId -> { }));

        assertThrows(IllegalStateException.class, () -> this.world.observer("GroupByTermObserver")
                .event(Flecs.OnAdd)
                .with(Position.class)
                .groupBy(region)
                .each(entityId -> { }));

        assertThrows(IllegalStateException.class, () -> this.world.observer("DetectChangesTermObserver")
                .event(Flecs.OnAdd)
                .with(Position.class)
                .detectChanges()
                .each(entityId -> { }));

        assertThrows(IllegalStateException.class, () -> this.world.observer("CascadeTermObserver")
                .event(Flecs.OnAdd)
                .with(Position.class).cascade()
                .each(entityId -> { }));

        assertThrows(IllegalStateException.class, () -> this.world.observer("ScopeTermObserver")
                .event(Flecs.OnAdd)
                .with(Position.class)
                .scopeOpen().not()
                .with(Velocity.class).or()
                .with(Position.class)
                .scopeClose()
                .each(entityId -> { }));

        assertThrows(IllegalStateException.class, () -> this.world.observer("GroupByCtxTermObserver")
                .event(Flecs.OnAdd)
                .with(Position.class)
                .groupBy(region)
                .groupByCtx(new Object())
                .each(entityId -> { }));
    }

    @Test
    void termStringVariables() {
        long rel = this.world.entity("ObserverStringVarRel");
        long target = this.world.entity("ObserverStringVarTarget");

        Observer variable = this.world.observer("StringVarObserver")
                .event(Flecs.OnAdd)
                .with(Position.class)
                .with("$ObserverVar")
                .each(entityId -> { });
        assertNotEquals(0, variable.id());

        Observer pair = this.world.observer("StringVarPairObserver")
                .event(Flecs.OnAdd)
                .with(Position.class)
                .with("$ObserverRel", target)
                .each(entityId -> { });
        assertNotEquals(0, pair.id());

        Observer secondVar = this.world.observer("StringVarSecondObserver")
                .event(Flecs.OnAdd)
                .with(Position.class)
                .with(rel).second("$ObserverTarget")
                .each(entityId -> { });
        assertNotEquals(0, secondVar.id());

        Observer secondString = this.world.observer("StringVarSecondStringObserver")
                .event(Flecs.OnAdd)
                .with(Position.class)
                .with(rel, "$ObserverTarget")
                .each(entityId -> { });
        assertNotEquals(0, secondString.id());
    }

    @Test
    void groupByCtxWithoutGroupBy() {
        Object ctx = new Object();

        Observer observer = this.world.observer("GroupCtxObserver")
                .event(Flecs.OnAdd)
                .with(Position.class)
                .groupByCtx(ctx)
                .each(entityId -> { });

        assertNotEquals(0, observer.id());
    }
}
