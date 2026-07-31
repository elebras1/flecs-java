# Flecs-Java

![Flecs](https://raw.githubusercontent.com/SanderMertens/flecs/master/docs/img/logo.png)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.elebras1/flecs-java)](https://central.sonatype.com/artifact/io.github.elebras1/flecs-java)
[![License: MIT](https://img.shields.io/github/license/elebras1/flecs-java)](https://github.com/elebras1/flecs-java/blob/main/LICENSE)
[![Java 25+](https://img.shields.io/badge/Java-25%2B-orange)](https://github.com/elebras1/flecs-java)
[![Documentation](https://img.shields.io/badge/docs-flecs.dev-blue)](https://www.flecs.dev/flecs/)

Java bindings for [Flecs](https://github.com/SanderMertens/flecs) (v4.1.6) - A fast and flexible Entity Component System (ECS) using Java 25's Foreign Function & Memory API (FFM).

- **Zero JNI overhead**: direct native calls via Project Panama's FFM API
- **Type-safe components**: define components as plain Java records, an annotation processor generates the memory layout and accessors
- **Multi-platform**: Linux, Windows, macOS (x86_64 and aarch64)

## Quick example

```java
@Component
record Position(float x, float y) {}

@Component
record Velocity(float dx, float dy) {}

World world = new World();
world.component(Position.class);
world.component(Velocity.class);

Entity player = world.obtainEntity(world.entity("Player"));
player.set(new Position(0, 0)).set(new Velocity(1, 0));

world.system("MoveSystem")
    .with(Position.class)
    .with(Velocity.class)
    .iter(it -> {
        Field<Position> positions = it.field(Position.class, 0);
        Field<Velocity> velocities = it.field(Velocity.class, 1);
        for (int i = 0; i < it.count(); i++) {
            PositionView p = positions.getMutView(i);
            VelocityView v = velocities.getMutView(i);
            p.x(p.x() + v.dx() * it.deltaTime());
            p.y(p.y() + v.dy() * it.deltaTime());
        }
    });

while (world.progress()) {}
```

Find many examples in [`examples/`](https://github.com/elebras1/flecs-java/blob/main/examples/src/main/java/io/github/elebras1/flecs/examples).

If this project is useful to you, consider giving it a ⭐, it helps others find it.

## Installation

### Gradle

```gradle
dependencies {
    implementation 'io.github.elebras1:flecs-java:0.11.2'
    annotationProcessor 'io.github.elebras1:flecs-java:0.11.2'
}
```

## Requirements

### Runtime
- **Java 25+**
- **Gradle 9+**

### Build from source
- GCC or compatible C compiler
- jextract-25 (only needed when regenerating FFM bindings)
- Supported architectures: Linux, Windows, macOS x86_64 and aarch64

## Documentation

- **[Flecs Manual](https://www.flecs.dev/flecs/)** official Flecs documentation
- **[Examples](https://github.com/elebras1/flecs-java/blob/main/examples/src/main/java/io/github/elebras1/flecs/examples)** code examples covering various features
- **[Tests](https://github.com/elebras1/flecs-java/tree/main/src/test/java/io/github/elebras1/flecs)** unit and integration test suite
- **[Benchmarks](https://github.com/elebras1/flecs-java/tree/main/benchmark)** performance benchmarks

## Architecture

Flecs-Java uses Java 25's Foreign Function & Memory API for direct C interop, with arena-based memory management and strongly-typed `MemorySegment` layouts. Components are defined as Java records with the `@Component` annotation; an annotation processor generates memory layouts and accessor code at compile time.

## Status

All core Flecs features are implemented. What may still be missing:
- Some convenience overloads (e.g. `method(long entity)` alongside `method(Entity entity)`)
- Minor or C-specific features that most language bindings don't typically implement
- Possibly feature that slipped through

Open an issue if you hit a gap.

## Building from source

```bash
git clone https://github.com/elebras1/flecs-java.git
cd flecs-java
./gradlew build
```

The build automatically downloads the Flecs C source, compiles the native library, runs annotation processing, and packages the JAR.

### Updating FFM bindings

```bash
# requires jextract-25 installed
./gradlew generateFlecsBindings
```

Regenerates bindings from `flecs.h` into `src/main/generated/`. Regular users don't need to run this.

## Contributing

Fork, create a feature branch, make your changes, submit a pull request. Or just open an issue, all contributions welcome!

## Support

- **Issues**: [GitHub Issues](https://github.com/elebras1/flecs-java/issues)
- **Flecs Discord**: [Join the community](https://discord.gg/flecs)

## License

Flecs-Java is licensed under the [MIT License](https://github.com/elebras1/flecs-java/blob/main/LICENSE). Flecs itself is also MIT-licensed, see the [Flecs repository](https://github.com/SanderMertens/flecs).