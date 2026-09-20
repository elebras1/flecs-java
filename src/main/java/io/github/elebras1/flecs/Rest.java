package io.github.elebras1.flecs;

public final class Rest {

    private int port;

    public Rest() {
    }

    public Rest port(int port) {
        this.port = port;
        return this;
    }

    public int port() {
        return this.port;
    }
}
