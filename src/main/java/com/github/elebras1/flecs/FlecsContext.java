package com.github.elebras1.flecs;

public class FlecsContext {
    private static final int BUFFER_SIZE = 48;
    private static final int MASK = BUFFER_SIZE - 1;
    private final ComponentViewPool[] componentViewPools;
    private final EntityView[] entityViewPool;
    private final ComponentRowViewPool[] componentRowViewPools;
    private int entityViewCursor;
    private int epoch;

    private static class ComponentViewPool {
        final ComponentView[] pool;
        int cursor;
        int epoch;

        ComponentViewPool(ComponentView[] pool) {
            this.pool = pool;
            this.cursor = 0;
            this.epoch = -1;
        }
    }

    private static class ComponentRowViewPool {
        final ComponentRowView[] pool;
        int cursor;
        int epoch;

        ComponentRowViewPool(ComponentRowView[] pool) {
            this.pool = pool;
            this.cursor = 0;
            this.epoch = -1;
        }
    }

    public FlecsContext(World world) {
        this.componentViewPools = new ComponentViewPool[ComponentMap.size()];
        this.entityViewPool = new EntityView[BUFFER_SIZE];
        this.componentRowViewPools = new ComponentRowViewPool[ComponentMap.size()];
        for (int i = 0; i < BUFFER_SIZE; i++) {
            this.entityViewPool[i] = new EntityView(world, 0);
        }
        this.entityViewCursor = 0;
        this.epoch = 0;
    }

    public EntityView getEntityView(long entityId) {
        EntityView entityView = this.entityViewPool[this.entityViewCursor];
        entityView.setId(entityId);
        this.entityViewCursor = (this.entityViewCursor + 1) & MASK;
        return entityView;
    }

    public ComponentView getComponentView(Class<?> componentClass) {
        int index = ComponentMap.getIndex(componentClass);
        ComponentViewPool viewPool = this.componentViewPools[index];

        if (viewPool == null) {
            ComponentView[] pool = new ComponentView[BUFFER_SIZE];
            for (int i = 0; i < BUFFER_SIZE; i++) {
                pool[i] = ComponentMap.getView(componentClass);
            }
            viewPool = new ComponentViewPool(pool);
            this.componentViewPools[index] = viewPool;
        }

        if (viewPool.epoch != this.epoch) {
            viewPool.cursor = 0;
            viewPool.epoch = this.epoch;
        }

        int cursor = viewPool.cursor;
        ComponentView view = viewPool.pool[cursor];
        viewPool.cursor = (cursor + 1) & MASK;
        return view;
    }

    public ComponentRowView getComponentRowView(Class<?> componentClass) {
        int index = ComponentMap.getIndex(componentClass);
        if (index <0) {
            return null;
        }

        ComponentRowViewPool viewPool = this.componentRowViewPools[index];
        if (viewPool == null) {
            ComponentRowView[] pool = new ComponentRowView[BUFFER_SIZE];
            for (int i =0; i < BUFFER_SIZE; i++) {
                pool[i] = ComponentMap.getRowView(componentClass);
            }
            viewPool = new ComponentRowViewPool(pool);
            this.componentRowViewPools[index] = viewPool;
        }

        if (viewPool.epoch != this.epoch) {
            viewPool.cursor =0;
            viewPool.epoch = this.epoch;
        }

        int cursor = viewPool.cursor;
        ComponentRowView view = viewPool.pool[cursor];
        viewPool.cursor = (cursor +1) % BUFFER_SIZE;
        return view;
    }

    public void resetCursors() {
        this.epoch++;
        this.entityViewCursor = 0;
    }
}