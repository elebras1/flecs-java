#include "benchmark.h"

#include <locale.h>
#include <stdio.h>

static void bench_run_entries(const BenchEntry *entries, int count, int n) {
    for (int i = 0; i < count; i++) {
        entries[i].run(n);
    }
}

int main(void) {
    setlocale(LC_NUMERIC, "C");

    static const int counts[] = { 1000, 10000, 100000 };

    for (int i = 0; i < (int)(sizeof(counts) / sizeof(counts[0])); i++) {
        bench_run_entries(tier1_benchmarks, tier1_benchmark_count, counts[i]);
        bench_run_entries(tier2_benchmarks, tier2_benchmark_count, counts[i]);
    }

    printf("DONE\n");
    fflush(stdout);
    return 0;
}
