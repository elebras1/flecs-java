package io.github.elebras1.flecs;

public final class Rest {

    private short port;

    public Rest() {
    }

    public Rest port(int port) {
        this.port = (short) port;
        return this;
    }

    public short port() {
        return this.port;
    }
}
