#ifndef BENCHMARK_H
#define BENCHMARK_H

#define _POSIX_C_SOURCE 200809L

#include <flecs.h>
#include <stdatomic.h>
#include <stddef.h>
#include <stdint.h>
#include <string.h>

#define BENCH_IMPL "flecs-c"
#ifdef BENCH_QUICK
#define BENCH_WARMUP_RUNS 5
#define BENCH_MEASURED_RUNS 7
#else
#define BENCH_WARMUP_RUNS 10
#define BENCH_MEASURED_RUNS 20
#endif
#define BENCH_SEED 42u

typedef struct {
    float x;
    float y;
} Position;

typedef struct {
    float dx;
    float dy;
} Velocity;

typedef struct {
} BenchTag;

typedef struct {
    int value;
} Health;

typedef struct {
    float value;
} Mass;

typedef struct {
    int value;
} Age;

extern ECS_COMPONENT_DECLARE(Position);
extern ECS_COMPONENT_DECLARE(Velocity);
extern ECS_TAG_DECLARE(Tag);
extern ECS_COMPONENT_DECLARE(Health);
extern ECS_COMPONENT_DECLARE(Mass);
extern ECS_COMPONENT_DECLARE(Age);

extern _Atomic unsigned long long bench_checksum;

void bench_sink_u64(unsigned long long value);

static inline uint32_t bench_f32_bits(float value) {
    uint32_t bits;
    memcpy(&bits, &value, sizeof(bits));
    return bits;
}

static inline void bench_sink_f32(float value) {
    bench_sink_u64((unsigned long long)bench_f32_bits(value));
}

typedef struct {
    const char *name;
    int n;
    void (*setup)(void *ctx);
    void (*before_run)(void *ctx);
    void (*run)(void *ctx);
    void (*after_run)(void *ctx);
    void (*fini)(void *ctx);
    void *ctx;
} Benchmark;

void bench_run_benchmark(const Benchmark *benchmark);

ecs_world_t *bench_world_new(void);
void bench_shuffle(int *order, int n, unsigned seed);

typedef struct {
    const char *name;
    void (*run)(int n);
} BenchEntry;

extern const BenchEntry tier1_benchmarks[];
extern const int tier1_benchmark_count;
extern const BenchEntry tier2_benchmarks[];
extern const int tier2_benchmark_count;

#endif
