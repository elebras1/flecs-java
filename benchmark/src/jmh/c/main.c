#include "benchmark.h"
#include "entity_creation_benchmark.h"
#include "query_benchmark.h"

#include <stdio.h>
#include <time.h>

static void print_header(void) {
    time_t now = time(NULL);
    char buf[32];
    printf("  Runs       : %d  |  Warmup: %d  |  Iterations: %d\n");
}

int main(void) {
    print_header();

    BenchmarkResult result;

    benchmark_create(&result);
    benchmark_print(&result);

    benchmark_create_with_2_components(&result);
    benchmark_print(&result);

    benchmark_create_with_2_components_from_prefab(&result);
    benchmark_print(&result);

    benchmark_query(&result);
    benchmark_print(&result);

    return 0;
}
