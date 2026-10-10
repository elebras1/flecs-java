#include "benchmark.h"

#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

ECS_COMPONENT_DECLARE(Position);
ECS_COMPONENT_DECLARE(Velocity);
ECS_TAG_DECLARE(Tag);
ECS_COMPONENT_DECLARE(Health);
ECS_COMPONENT_DECLARE(Mass);
ECS_COMPONENT_DECLARE(Age);

_Atomic unsigned long long bench_checksum = 0;

void bench_sink_u64(unsigned long long value) {
    atomic_fetch_add_explicit(&bench_checksum,
        (value ^ 0x9e3779b97f4a7c15ULL) + 0x100000001b3ULL,
        memory_order_relaxed);
}

static double bench_now_ns(void) {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (double)ts.tv_sec * 1e9 + (double)ts.tv_nsec;
}

ecs_world_t *bench_world_new(void) {
    ecs_world_t *world = ecs_init();
    ECS_COMPONENT_DEFINE(world, Position);
    ECS_COMPONENT_DEFINE(world, Velocity);
    ECS_TAG_DEFINE(world, Tag);
    ECS_COMPONENT_DEFINE(world, Health);
    ECS_COMPONENT_DEFINE(world, Mass);
    ECS_COMPONENT_DEFINE(world, Age);
    return world;
}

static uint64_t bench_splitmix64(uint64_t *state) {
    uint64_t z = (*state += 0x9e3779b97f4a7c15ULL);
    z = (z ^ (z >> 30)) * 0xbf58476d1ce4e5b9ULL;
    z = (z ^ (z >> 27)) * 0x94d049bb133111ebULL;
    return z ^ (z >> 31);
}

void bench_shuffle(int *order, int n, unsigned seed) {
    for (int i = 0; i < n; i++) {
        order[i] = i;
    }

    uint64_t state = (uint64_t)seed;
    for (int i = n - 1; i > 0; i--) {
        int j = (int)(bench_splitmix64(&state) % (uint64_t)(i + 1));
        int tmp = order[i];
        order[i] = order[j];
        order[j] = tmp;
    }
}

static int bench_compare_double(const void *a, const void *b) {
    double da = *(const double *)a;
    double db = *(const double *)b;
    return (da > db) - (da < db);
}

void bench_run_benchmark(const Benchmark *benchmark) {
    if (benchmark->setup) {
        benchmark->setup(benchmark->ctx);
    }

    for (int i = 0; i < BENCH_WARMUP_RUNS; i++) {
        if (benchmark->before_run) benchmark->before_run(benchmark->ctx);
        benchmark->run(benchmark->ctx);
        if (benchmark->after_run) benchmark->after_run(benchmark->ctx);
    }

    double samples[BENCH_MEASURED_RUNS];
    for (int i = 0; i < BENCH_MEASURED_RUNS; i++) {
        if (benchmark->before_run) benchmark->before_run(benchmark->ctx);
        double start = bench_now_ns();
        benchmark->run(benchmark->ctx);
        double end = bench_now_ns();
        if (benchmark->after_run) benchmark->after_run(benchmark->ctx);
        samples[i] = end - start;
    }

    if (benchmark->fini) {
        benchmark->fini(benchmark->ctx);
    }

    double sorted[BENCH_MEASURED_RUNS];
    memcpy(sorted, samples, sizeof(samples));
    qsort(sorted, BENCH_MEASURED_RUNS, sizeof(double), bench_compare_double);

    double median = (sorted[BENCH_MEASURED_RUNS / 2 - 1] +
                     sorted[BENCH_MEASURED_RUNS / 2]) / 2.0;

    double sum = 0.0;
    for (int i = 0; i < BENCH_MEASURED_RUNS; i++) sum += samples[i];
    double mean = sum / BENCH_MEASURED_RUNS;

    double variance = 0.0;
    for (int i = 0; i < BENCH_MEASURED_RUNS; i++) {
        double diff = samples[i] - mean;
        variance += diff * diff;
    }
    variance /= (BENCH_MEASURED_RUNS - 1);
    double stddev = sqrt(variance);

    unsigned long long checksum =
        atomic_load_explicit(&bench_checksum, memory_order_relaxed);
    double ns_per_unit = median / (double)benchmark->n;

    printf("RESULT %s %s %d %.6f %llu\n",
        benchmark->name, BENCH_IMPL, benchmark->n, ns_per_unit, checksum);
    printf("STAT %s %s %d mean_ns_per_unit %.6f stddev_ns %.3f runs %d\n",
        benchmark->name, BENCH_IMPL, benchmark->n, mean / (double)benchmark->n,
        stddev, BENCH_MEASURED_RUNS);
    fflush(stdout);
}
