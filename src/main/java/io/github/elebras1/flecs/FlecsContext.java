package io.github.elebras1.flecs;

import java.util.Arrays;

public class FlecsContext {
    private static final int BUFFER_SIZE = 64;
    private static final int MASK = BUFFER_SIZE - 1;
    private static final int CURSOR_BITS = Integer.numberOfTrailingZeros(BUFFER_SIZE);
    private static final int LEVELS = 16;

    private final ComponentViewPool[] componentViewPools;
    private final ComponentMutViewPool[] componentMutViewPools;
    private final ComponentRowViewPool[] componentRowViewPools;
    private final EntityViewPool entityViewPool;

    private int scopeDepth;
    private int scopeLevel;

    private abstract static class Pool {
        final int[] cursors = new int[LEVELS];
        int levels;

        abstract void addLevel(int level);
    }

    private static final class EntityViewPool extends Pool {
        final World world;
        EntityView[] items;

        EntityViewPool(World world) {
            this.world = world;
            this.items = new EntityView[BUFFER_SIZE];
            for (int i = 0; i < BUFFER_SIZE; i++) {
                this.items[i] = new EntityView(world, 0);
            }
            this.levels = 1;
        }

        @Override
        void addLevel(int level) {
            int from = this.levels << CURSOR_BITS;
            int to = (level + 1) << CURSOR_BITS;
            EntityView[] grown = Arrays.copyOf(this.items, to);
            for (int i = from; i < to; i++) {
                grown[i] = new EntityView(this.world, 0);
            }
            this.items = grown;
            this.levels = level + 1;
        }
    }

    private static final class ComponentViewPool extends Pool {
        final Class<?> componentClass;
        ComponentView[] items;

        ComponentViewPool(Class<?> componentClass) {
            this.componentClass = componentClass;
            this.items = new ComponentView[BUFFER_SIZE];
            for (int i = 0; i < BUFFER_SIZE; i++) {
                this.items[i] = ComponentMap.getView(componentClass);
            }
            this.levels = 1;
        }

        @Override
        void addLevel(int level) {
            int from = this.levels << CURSOR_BITS;
            int to = (level + 1) << CURSOR_BITS;
            ComponentView[] grown = Arrays.copyOf(this.items, to);
            for (int i = from; i < to; i++) {
                grown[i] = ComponentMap.getView(this.componentClass);
            }
            this.items = grown;
            this.levels = level + 1;
        }
    }

    private static final class ComponentMutViewPool extends Pool {
        final Class<?> componentClass;
        ComponentMutView[] items;

        ComponentMutViewPool(Class<?> componentClass) {
            this.componentClass = componentClass;
            this.items = new ComponentMutView[BUFFER_SIZE];
            for (int i = 0; i < BUFFER_SIZE; i++) {
                this.items[i] = ComponentMap.getMutView(componentClass);
            }
            this.levels = 1;
        }

        @Override
        void addLevel(int level) {
            int from = this.levels << CURSOR_BITS;
            int to = (level + 1) << CURSOR_BITS;
            ComponentMutView[] grown = Arrays.copyOf(this.items, to);
            for (int i = from; i < to; i++) {
                grown[i] = ComponentMap.getMutView(this.componentClass);
            }
            this.items = grown;
            this.levels = level + 1;
        }
    }

    private static final class ComponentRowViewPool extends Pool {
        final Class<?> componentClass;
        ComponentRowView[] items;

        ComponentRowViewPool(Class<?> componentClass) {
            this.componentClass = componentClass;
            this.items = new ComponentRowView[BUFFER_SIZE];
            for (int i = 0; i < BUFFER_SIZE; i++) {
                this.items[i] = ComponentMap.getRowView(componentClass);
            }
            this.levels = 1;
        }

        @Override
        void addLevel(int level) {
            int from = this.levels << CURSOR_BITS;
            int to = (level + 1) << CURSOR_BITS;
            ComponentRowView[] grown = Arrays.copyOf(this.items, to);
            for (int i = from; i < to; i++) {
                grown[i] = ComponentMap.getRowView(this.componentClass);
            }
            this.items = grown;
            this.levels = level + 1;
        }
    }

    public FlecsContext(World world) {
        int size = ComponentMap.size();
        this.componentViewPools = new ComponentViewPool[size];
        this.componentMutViewPools = new ComponentMutViewPool[size];
        this.componentRowViewPools = new ComponentRowViewPool[size];
        this.entityViewPool = new EntityViewPool(world);
        this.scopeDepth = 0;
        this.scopeLevel = 0;
    }

    public void enterIteration() {
        int depth = this.scopeDepth + 1;
        this.scopeDepth = depth;
        this.scopeLevel = depth < LEVELS ? depth : LEVELS - 1;
    }

    public void exitIteration() {
        int depth = this.scopeDepth;
        if (depth == 0) {
            return;
        }
        depth--;
        this.scopeDepth = depth;
        this.scopeLevel = depth < LEVELS ? depth : LEVELS - 1;
    }

    private int slot(Pool pool) {
        int level = this.scopeLevel;
        if (level >= pool.levels) {
            pool.addLevel(level);
        }
        int[] cursors = pool.cursors;
        int c = cursors[level];
        cursors[level] = (c + 1) & MASK;
        return (level << CURSOR_BITS) | c;
    }

    public EntityView getEntityView(long entityId) {
        EntityViewPool pool = this.entityViewPool;
        int s = this.slot(pool);
        EntityView view = pool.items[s];
        view.id = entityId;
        return view;
    }

    public ComponentView getComponentView(Class<?> componentClass) {
        return this.getComponentView(ComponentMap.getIndex(componentClass), componentClass);
    }

    public ComponentView getComponentView(int index, Class<?> componentClass) {
        if (index < 0) {
            return null;
        }
        ComponentViewPool pool = this.componentViewPools[index];
        if (pool == null) {
            pool = new ComponentViewPool(componentClass);
            this.componentViewPools[index] = pool;
        }
        int s = this.slot(pool);
        return pool.items[s];
    }

    public ComponentMutView getComponentMutView(Class<?> componentClass) {
        return this.getComponentMutView(ComponentMap.getIndex(componentClass), componentClass);
    }

    public ComponentMutView getComponentMutView(int index, Class<?> componentClass) {
        if (index < 0) {
            return null;
        }
        ComponentMutViewPool pool = this.componentMutViewPools[index];
        if (pool == null) {
            pool = new ComponentMutViewPool(componentClass);
            this.componentMutViewPools[index] = pool;
        }
        int s = this.slot(pool);
        return pool.items[s];
    }

    public ComponentMutView acquireComponentMutView(Class<?> componentClass) {
        return this.getComponentMutView(componentClass);
    }

    public ComponentMutView acquireComponentMutView(int index, Class<?> componentClass) {
        return this.getComponentMutView(index, componentClass);
    }

    public ComponentRowView getComponentRowView(Class<?> componentClass) {
        return this.getComponentRowView(ComponentMap.getIndex(componentClass), componentClass);
    }

    public ComponentRowView getComponentRowView(int index, Class<?> componentClass) {
        if (index < 0) {
            return null;
        }
        ComponentRowViewPool pool = this.componentRowViewPools[index];
        if (pool == null) {
            pool = new ComponentRowViewPool(componentClass);
            this.componentRowViewPools[index] = pool;
        }
        int s = this.slot(pool);
        return pool.items[s];
    }
}