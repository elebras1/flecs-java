package com.github.elebras1.flecs;

import java.util.IdentityHashMap;
import java.util.Map;

public class FlecsContext {
    public static final ScopedValue<ViewCache> CURRENT_CACHE = ScopedValue.newInstance();

    public static class ViewCache {
        private final Map<Class<?>, ComponentView> views;
        private final EntityView entityView;

        public ViewCache() {
            this.views = new IdentityHashMap<>(8);
            this.entityView = new EntityView(null, 0);
        }

        public ComponentView get(Class<?> componentClass) {
            ComponentView view = this.views.get(componentClass);
            if (view != null) {
                return view;
            }

            view = ComponentMap.getView(componentClass);

            if (view != null) {
                this.views.put(componentClass, view);
            }

            return view;
        }

        public EntityView getEntityView() {
            return this.entityView;
        }
    }
}