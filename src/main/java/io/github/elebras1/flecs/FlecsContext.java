package io.github.elebras1.flecs;

public class FlecsContext {
    private static final int BUFFER_SIZE = 64;
    private static final int MASK = BUFFER_SIZE - 1;
    private final ComponentViewPool[] componentViewPools;
    private final ComponentMutViewPool[] componentMutViewPools;
    private final EntityView[] entityViewPool;
    private final ComponentRowViewPool[] componentRowViewPools;
    private int entityViewCursor;
    private int[] scopeFrameBase;
    private int[] scopePairIndex;
    private int[] scopePairStart;
    private final int[] scopeTouched;
    private int scopePairCount;
    private int scopeDepth;
    private int scopeFrameId;

    private static class ComponentViewPool {
        final ComponentView[] pool;
        int cursor;

        ComponentViewPool(ComponentView[] pool) {
            this.pool = pool;
            this.cursor = 0;
        }
    }

    private static class ComponentMutViewPool {
        final ComponentMutView[] pool;
        int cursor;

        ComponentMutViewPool(ComponentMutView[] pool) {
            this.pool = pool;
            this.cursor = 0;
        }
    }

    private static class ComponentRowViewPool {
        final ComponentRowView[] pool;
        int cursor;

        ComponentRowViewPool(ComponentRowView[] pool) {
            this.pool = pool;
            this.cursor = 0;
        }
    }

    public FlecsContext(World world) {
        this.componentViewPools = new ComponentViewPool[ComponentMap.size()];
        this.componentMutViewPools = new ComponentMutViewPool[ComponentMap.size()];
        this.entityViewPool = new EntityView[BUFFER_SIZE];
        this.componentRowViewPools = new ComponentRowViewPool[ComponentMap.size()];
        for (int i = 0; i < BUFFER_SIZE; i++) {
            this.entityViewPool[i] = new EntityView(world, 0);
        }
        this.entityViewCursor = 0;
        this.scopeFrameBase = new int[8];
        this.scopePairIndex = new int[16];
        this.scopePairStart = new int[16];
        this.scopeTouched = new int[ComponentMap.size()];
        this.scopePairCount = 0;
        this.scopeDepth = 0;
        this.scopeFrameId = 0;
    }

    public EntityView getEntityView(long entityId) {
        EntityView entityView = this.entityViewPool[this.entityViewCursor];
        entityView.id = entityId;
        this.entityViewCursor = (this.entityViewCursor + 1) & MASK;
        return entityView;
    }

    public ComponentView getComponentView(Class<?> componentClass) {
        return this.getComponentView(ComponentMap.getIndex(componentClass), componentClass);
    }

    public ComponentView getComponentView(int index, Class<?> componentClass) {
        if (index < 0) {
            return null;
        }
        ComponentViewPool viewPool = this.componentViewPools[index];

        if (viewPool == null) {
            ComponentView[] pool = new ComponentView[BUFFER_SIZE];
            for (int i = 0; i < BUFFER_SIZE; i++) {
                pool[i] = ComponentMap.getView(componentClass);
            }
            viewPool = new ComponentViewPool(pool);
            this.componentViewPools[index] = viewPool;
        }

        int cursor = viewPool.cursor;
        ComponentView view = viewPool.pool[cursor];
        viewPool.cursor = (cursor + 1) & MASK;
        return view;
    }

    public ComponentMutView getComponentMutView(Class<?> componentClass) {
        return this.getComponentMutView(ComponentMap.getIndex(componentClass), componentClass);
    }

    public ComponentMutView getComponentMutView(int index, Class<?> componentClass) {
        if (index < 0) {
            return null;
        }
        ComponentMutViewPool viewPool = this.componentMutViewPools[index];

        if (viewPool == null) {
            ComponentMutView[] pool = new ComponentMutView[BUFFER_SIZE];
            for (int i = 0; i < BUFFER_SIZE; i++) {
                pool[i] = ComponentMap.getMutView(componentClass);
            }
            viewPool = new ComponentMutViewPool(pool);
            this.componentMutViewPools[index] = viewPool;
        }

        int cursor = viewPool.cursor;
        ComponentMutView view = viewPool.pool[cursor];
        viewPool.cursor = (cursor + 1) & MASK;
        return view;
    }

    public ComponentMutView acquireComponentMutView(Class<?> componentClass) {
        return this.acquireComponentMutView(ComponentMap.getIndex(componentClass), componentClass);
    }

    public ComponentMutView acquireComponentMutView(int index, Class<?> componentClass) {
        if (index < 0) {
            return null;
        }

        ComponentMutViewPool viewPool = this.componentMutViewPools[index];
        if (viewPool == null) {
            ComponentMutView[] pool = new ComponentMutView[BUFFER_SIZE];
            for (int i = 0; i < BUFFER_SIZE; i++) {
                pool[i] = ComponentMap.getMutView(componentClass);
            }
            viewPool = new ComponentMutViewPool(pool);
            this.componentMutViewPools[index] = viewPool;
        }

        if (this.scopeDepth > 0 && this.scopeTouched[index] != this.scopeFrameId) {
            this.scopeTouched[index] = this.scopeFrameId;
            if (this.scopePairCount == this.scopePairIndex.length) {
                int size = this.scopePairIndex.length * 2;
                this.scopePairIndex = java.util.Arrays.copyOf(this.scopePairIndex, size);
                this.scopePairStart = java.util.Arrays.copyOf(this.scopePairStart, size);
            }
            this.scopePairIndex[this.scopePairCount] = index;
            this.scopePairStart[this.scopePairCount] = viewPool.cursor;
            this.scopePairCount++;
        }

        int cursor = viewPool.cursor;
        ComponentMutView view = viewPool.pool[cursor];
        viewPool.cursor = (cursor + 1) & MASK;
        return view;
    }

    public void enterIteration() {
        if (this.scopeDepth == this.scopeFrameBase.length) {
            this.scopeFrameBase = java.util.Arrays.copyOf(this.scopeFrameBase, this.scopeDepth * 2);
        }
        this.scopeFrameId++;
        this.scopeFrameBase[this.scopeDepth++] = this.scopePairCount;
    }

    public void exitIteration() {
        if (this.scopeDepth == 0) {
            return;
        }
        int base = this.scopeFrameBase[--this.scopeDepth];
        for (int i = this.scopePairCount - 1; i >= base; i--) {
            ComponentMutViewPool viewPool = this.componentMutViewPools[this.scopePairIndex[i]];
            if (viewPool != null) {
                viewPool.cursor = this.scopePairStart[i];
            }
        }
        this.scopePairCount = base;
    }

    public ComponentRowView getComponentRowView(Class<?> componentClass) {
        return this.getComponentRowView(ComponentMap.getIndex(componentClass), componentClass);
    }

    public ComponentRowView getComponentRowView(int index, Class<?> componentClass) {
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

        int cursor = viewPool.cursor;
        ComponentRowView view = viewPool.pool[cursor];
        viewPool.cursor = (cursor +1) & MASK;
        return view;
    }
}