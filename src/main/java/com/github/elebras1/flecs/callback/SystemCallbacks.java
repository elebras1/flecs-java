package com.github.elebras1.flecs.callback;

public record SystemCallbacks(IterCallback iterCallback, RunCallback runCallback, EntityCallback entityCallback) {
}