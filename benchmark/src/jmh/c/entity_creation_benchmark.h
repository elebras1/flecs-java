#ifndef ENTITY_CREATION_BENCHMARK_H
#define ENTITY_CREATION_BENCHMARK_H

#include "benchmark.h"

void benchmark_create(BenchmarkResult *out);
void benchmark_create_with_2_components(BenchmarkResult *out);
void benchmark_create_with_2_components_from_prefab(BenchmarkResult *out);

#endif
