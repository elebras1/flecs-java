#ifndef BENCHMARK_H
#define BENCHMARK_H

#define _POSIX_C_SOURCE 199309L

#include <flecs.h>
#include <stddef.h>

#define BENCH_ITERATIONS 100000
#define BENCH_RUNS 50
#define BENCH_WARMUP_RUNS 5

typedef struct {
    int value;
} Health;

typedef struct {
    int color;
    int factionDriftingSpeed;
    int stability;
} Ideology;

extern ECS_COMPONENT_DECLARE(Health);
extern ECS_COMPONENT_DECLARE(Ideology);

typedef struct {
    const char *name;
    double avg_us;
    double per_op_us;
    double stddev_us;
    double error_us;
    int runs;
    int iterations;
} BenchmarkResult;

double benchmark_time_us(void);
void benchmark_print(const BenchmarkResult *result);


void benchmark_run(
    const char *name,
    void (*setup)(void *ctx),
    void (*teardown)(void *ctx),
    void (*fn)(void *ctx),
    void *ctx,
    BenchmarkResult *out
);

ecs_world_t *benchmark_world_new(void);

void benchmark_world_populate(ecs_world_t *world, int count);

#endif
