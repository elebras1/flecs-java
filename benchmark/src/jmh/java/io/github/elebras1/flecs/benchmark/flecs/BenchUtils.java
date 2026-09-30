package io.github.elebras1.flecs.benchmark.flecs;

final class BenchUtils {

    static final long SEED = 42L;

    private BenchUtils() {
    }

    static int[] shuffledIndices(int n) {
        int[] order = new int[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }
        long state = SEED;
        for (int i = n - 1; i > 0; i--) {
            state += 0x9e3779b97f4a7c15L;
            long z = state;
            z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
            z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
            z ^= z >>> 31;
            int j = (int) Long.remainderUnsigned(z, i + 1L);
            int tmp = order[i];
            order[i] = order[j];
            order[j] = tmp;
        }
        return order;
    }
}
