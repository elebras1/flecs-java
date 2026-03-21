package com.github.elebras1.flecs.callback;

public record ObserverCallbacks(IterCallback iterCallback, RunCallback runCallback, EntityCallback entityCallback) {
}