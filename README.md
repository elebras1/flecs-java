# Flecs-Java

![Flecs](https://raw.githubusercontent.com/SanderMertens/flecs/master/docs/img/logo.png)

Java bindings for [Flecs](https://github.com/SanderMertens/flecs) - A fast and flexible Entity Component System (ECS) using Java 25's Foreign Function & Memory API (FFM).

## What is Flecs?

Flecs is a powerful ECS framework written in C that provides high-performance data-oriented programming. This wrapper brings Flecs capabilities to Java while using Project Panama's FFM API.

- **Multi-Platform**: Support for Linux, Windows, and macOS

## Requirements

### Runtime
- **Java 25+**
- **Gradle 9+**

### Build from Source
- **GCC or compatible C compiler** (for compiling the native Flecs library)
- **jextract-25** (for generating Java FFM bindings when updating Flecs version)
- **Supported Architectures**: 
  - Linux: x86_64, aarch64
  - Windows: x86_64, aarch64
  - macOS: x86_64, aarch64

## Installation

### Gradle

```gradle
dependencies {
    implementation 'io.github.elebras1:flecs-java:0.5.3'
    annotationProcessor 'io.github.elebras1:flecs-java:0.5.3'

}
```

### Build from Source

```bash
# Clone repository
git clone https://github.com/elebras1/flecs-java.git
cd flecs-java

# Build (downloads Flecs, compiles natives)
./gradlew build

# Run examples
./gradlew :examples:run
```

## Example

```java
import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.annotation.Component;

// Define components as records
@Component
record Position(float x, float y) {}

@Component
record Velocity(float dx, float dy) {}

public class Example {
    public static void main(String[] args) {
        try (Flecs world = new Flecs()) {
            // Register components
            world.component(Position.class);
            world.component(Velocity.class);
            
            // Create entities
            Entity player = world.obtainEntity(world.entity("Player"));
            player.set(new Position(0, 0))
                  .set(new Velocity(1, 0));
            
            Entity enemy = world.obtainEntity(world.entity("Enemy"));
            enemy.set(new Position(10, 5))
                 .set(new Velocity(-0.5f, 0));

            // Set number of worker threads
            world.setThreads(4);
            
            // Create a movement system
            world.system("MoveSystem")
                .kind(FlecsConstants.EcsOnUpdate)
                .with(Position.class)
                .with(Velocity.class)
                .multithreaded()
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    Field<Velocity> velocities = it.field(Velocity.class, 1);
                    
                    for (int i = 0; i < it.count(); i++) {
                        PositionView positionView = positions.getMutView(i);
                        VelocityView velocityView = velocities.getMutView(i);
                        
                        // Update position
                        positionView.x(positionView.x() + velocityView.dx() * it.deltaTime());
                        positionView.y(positionView.y() + velocityView.dy() * it.deltaTime());
                    }
                });
            
            // Run simulation
            for (int i = 0; i < 10; i++) {
                world.progress(0.016f); // 60 FPS
            }
            
            // Query entities
            try (Query query = world.query().with(Position.class).build()) {
                query.each(entityId -> {
                    Entity e = world.obtainEntity(entityId);
                    Position pos = e.get(Position.class);
                    System.out.printf("%s: (%.2f, %.2f)%n", e.getName(), pos.x(), pos.y());
                });
            }
        }
    }
}
```

## Documentation

- **[Flecs Manual](https://www.flecs.dev/flecs/)** - Official Flecs documentation
- **[Examples](examples/src/main/java/com/github/elebras1/flecs/examples/)** - Code examples covering various features

## Architecture

### FFM API Integration

Flecs-Java uses Java 25's Foreign Function & Memory API for direct C interop:
- **Zero JNI overhead**: Direct native calls without marshalling
- **Memory safety**: Arena-based memory management
- **Type safety**: Strong typing with `MemorySegment` and layouts

### Component System

Components are defined as Java records with the `@Component` annotation. An annotation processor generates the necessary memory layouts and accessor code at compile time.

## Building

### Build Process Overview

The build process automatically handles the following steps:

1. **Download Flecs C Source** (`downloadFlecs`)
2. **Compile Native Library** (`compileFlecsNative`)
3. **Compile Annotation Processor** (`compileProcessor`)
4. **Generate Java Source** (Annotation Processing Phase)
5. **Package JAR**
6. **Runtime Native Loading**

### Updating FFM Bindings

When updating the Flecs version, maintainers must regenerate the FFM bindings:

```bash
# (requires jextract-25 installed)
./gradlew generateFlecsBindings
```

This generates the Java FFM interface bindings from `flecs.h` and stores them in `src/main/generated/`. Regular users don't need to run this task.

## Contributing

This wrapper currently implements core ECS functionality but does not yet support all Flecs features.
Feel free to open an issue or pull request. All contributions are welcome!

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

Or just report issues you encounter!

## Support

- **Issues**: [GitHub Issues](https://github.com/elebras1/flecs-java/issues)
- **Flecs Discord**: [Join the community](https://discord.gg/flecs)

## License

Flecs-Java is licensed under the [MIT License](LICENSE).

Flecs (the underlying C library) is also licensed under the MIT License. See the [Flecs repository](https://github.com/SanderMertens/flecs) for details.
