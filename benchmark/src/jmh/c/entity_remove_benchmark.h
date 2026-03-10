#ifndef ENTITY_REMOVE_BENCHMARK_H
#define ENTITY_REMOVE_BENCHMARK_H

#include "benchmark.h"

void benchmark_destruct_with_2_components(BenchmarkResult *out);

void benchmark_remove_1_components(BenchmarkResult *out);

void benchmark_remove_2_components(BenchmarkResult *out);

#endif
