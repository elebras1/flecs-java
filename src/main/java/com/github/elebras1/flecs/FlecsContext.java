package com.github.elebras1.flecs;

import java.util.IdentityHashMap;
import java.util.Map;

public class FlecsContext {
    public static final ScopedValue<ViewCache> CURRENT_CACHE = ScopedValue.newInstance();

    public static class ViewCache {
        private static final int BUFFER_SIZE = 16;
        private static final int MASK = BUFFER_SIZE - 1;
        private final Map<Class<?>, ComponentView[]> componentViewPools;
        private final Map<Class<?>, int[]> componentViewCursors;
        private final EntityView[] entityViewPool;
        private int entityViewCursor;

        public ViewCache(World world) {
            this.componentViewPools = new IdentityHashMap<>();
            this.componentViewCursors = new IdentityHashMap<>();
            this.entityViewPool = new EntityView[BUFFER_SIZE];
            for (int i = 0; i < BUFFER_SIZE; i++) {
                this.entityViewPool[i] = new EntityView(world, 0);
            }
            this.entityViewCursor = 0;
        }

        public EntityView getEntityView(long entityId) {
            EntityView entityView = this.entityViewPool[this.entityViewCursor];
            entityView.setId(entityId);
            this.entityViewCursor = (this.entityViewCursor + 1) & MASK;
            return entityView;
        }

        public ComponentView getComponentView(Class<?> componentClass) {
            ComponentView[] pool = this.componentViewPools.get(componentClass);

            if (pool == null) {
                pool = new ComponentView[BUFFER_SIZE];
                for (int i = 0; i < BUFFER_SIZE; i++) {
                    pool[i] = ComponentMap.getView(componentClass);
                }
                this.componentViewPools.put(componentClass, pool);
                this.componentViewCursors.put(componentClass, new int[]{0});
            }

            int[] cursorRef = this.componentViewCursors.get(componentClass);
            int index = cursorRef[0];

            ComponentView view = pool[index];
            cursorRef[0] = (index + 1) & MASK;

            return view;
        }
    }
}