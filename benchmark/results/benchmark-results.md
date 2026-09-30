# Benchmark results

Generated: 2026-09-30 20:37:45  
Git commit: `72985a7`  
Mode: **QUICK (reduced warmup/iterations, draft)**

> **Draft run.** Generated with `-Pquick`: fewer warmup and measurement
> iterations. Use it to validate the harness, not to compare numbers;
> rerun without `-Pquick` for publishable results.

## Machine

| Key | Value |
|---|---|
| os | Linux 7.0.0-28-generic |
| kernel | 7.0.0-28-generic |
| cpu | AMD Ryzen 5 5600GT with Radeon Graphics |
| cores | 12 |
| ram_gb | 15 |
| jvm | Oracle Corporation 25.0.3 |
| gcc | gcc (Ubuntu 13.3.0-6ubuntu2~24.04.1) 13.3.0 |

## Library versions

| Library | Version |
|---|---|
| flecs C | 4.1.6 |
| flecs-java | 0.1-SNAPSHOT |
| artemis-odb | 2.3.0 |
| dominion-ecs | 0.9.0 |

All values in the tables below are ns per unit (JMH average for Java,
median of the measured runs for C). The value in parentheses is the
ratio versus flecs C (lower is faster). Tier 1 compares flecs-java,
artemis-odb and dominion-ecs against flecs C; Tier 2 is flecs-specific.

## Tier 1

### N = 1,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| createEmpty | 8.776 (1.00x) | 17.430 (1.99x) | 10.239 (1.17x) | 16.950 (1.93x) | 1.99 |
| create1 | 50.182 (1.00x) | 107.351 (2.14x) | 25.898 (0.52x) | 27.636 (0.55x) | 2.14 |
| create2 | 94.849 (1.00x) | 198.667 (2.09x) | 39.778 (0.42x) | 37.998 (0.40x) | 2.09 |
| createFromPrefab | 104.570 (1.00x) | 204.413 (1.95x) | 33.554 (0.32x) | 61.979 (0.59x) | 1.95 |
| destroyEmpty | 20.266 (1.00x) | 26.886 (1.33x) | 12.783 (0.63x) | 18.346 (0.91x) | 1.33 |
| destroy2 | 25.016 (1.00x) | 30.285 (1.21x) | 19.548 (0.78x) | 29.240 (1.17x) | 1.21 |
| addComponent | 36.691 (1.00x) | 61.376 (1.67x) | 17.687 (0.48x) | 54.426 (1.48x) | 1.67 |
| removeComponent | 34.087 (1.00x) | 55.064 (1.62x) | 33.830 (0.99x) | 55.325 (1.62x) | 1.62 |
| get | 3.455 (1.00x) | 18.768 (5.43x) | 4.196 (1.21x) | 3.202 (0.93x) | 5.43 |
| getSet | 4.911 (1.00x) | 20.681 (4.21x) | 4.838 (0.99x) | 3.472 (0.71x) | 4.21 |
| has | 1.500 (1.00x) | 13.148 (8.77x) | 3.201 (2.13x) | 2.692 (1.79x) | 8.77 |
| query1Read | 0.260 (1.00x) | 0.863 (3.32x) | 0.852 (3.28x) | 0.738 (2.84x) | 3.32 |
| query2ReadWrite | 0.360 (1.00x) | 1.527 (4.24x) | 1.674 (4.65x) | 1.854 (5.15x) | 4.24 |
| queryFiltered | 0.260 (1.00x) | 0.865 (3.33x) | 1.019 (3.92x) | 0.853 (3.28x) | 3.33 |
| systemRun | 0.350 (1.00x) | 1.655 (4.73x) | 1.966 (5.62x) | 19.365 (55.33x) | 4.73 |
| queryCreate | 220.220 (1.00x) | 13944.665 (63.32x) | n/a | n/a | 63.32 |
| lookup | 63.343 (1.00x) | 93.651 (1.48x) | n/a | n/a | 1.48 |
| mixedSimulation | 11.486 (1.00x) | 24.269 (2.11x) | 9.609 (0.84x) | 8.678 (0.76x) | 2.11 |

### N = 10,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| createEmpty | 7.461 (1.00x) | 17.264 (2.31x) | 12.741 (1.71x) | 17.481 (2.34x) | 2.31 |
| create1 | 47.228 (1.00x) | 103.383 (2.19x) | 28.408 (0.60x) | 28.227 (0.60x) | 2.19 |
| create2 | 90.478 (1.00x) | 197.811 (2.19x) | 42.287 (0.47x) | 42.972 (0.47x) | 2.19 |
| createFromPrefab | 101.154 (1.00x) | 202.679 (2.00x) | 36.573 (0.36x) | 65.599 (0.65x) | 2.00 |
| destroyEmpty | 25.736 (1.00x) | 36.890 (1.43x) | 14.652 (0.57x) | 25.824 (1.00x) | 1.43 |
| destroy2 | 31.757 (1.00x) | 38.207 (1.20x) | 21.520 (0.68x) | 49.696 (1.56x) | 1.20 |
| addComponent | 47.176 (1.00x) | 82.540 (1.75x) | 19.310 (0.41x) | 71.076 (1.51x) | 1.75 |
| removeComponent | 45.814 (1.00x) | 78.234 (1.71x) | 45.505 (0.99x) | 72.301 (1.58x) | 1.71 |
| get | 4.935 (1.00x) | 25.930 (5.25x) | 5.425 (1.10x) | 6.435 (1.30x) | 5.25 |
| getSet | 6.284 (1.00x) | 26.606 (4.23x) | 5.484 (0.87x) | 6.181 (0.98x) | 4.23 |
| has | 1.924 (1.00x) | 14.377 (7.47x) | 5.627 (2.92x) | 5.321 (2.77x) | 7.47 |
| query1Read | 0.173 (1.00x) | 0.697 (4.03x) | 1.090 (6.30x) | 1.409 (8.14x) | 4.03 |
| query2ReadWrite | 0.269 (1.00x) | 1.568 (5.84x) | 1.780 (6.63x) | 2.950 (10.99x) | 5.84 |
| queryFiltered | 0.176 (1.00x) | 0.694 (3.95x) | 0.968 (5.51x) | 1.724 (9.82x) | 3.95 |
| systemRun | 0.271 (1.00x) | 1.794 (6.62x) | 2.146 (7.92x) | 4.215 (15.55x) | 6.62 |
| queryCreate | 220.936 (1.00x) | 13655.028 (61.81x) | n/a | n/a | 61.81 |
| lookup | 93.889 (1.00x) | 143.702 (1.53x) | n/a | n/a | 1.53 |
| mixedSimulation | 11.157 (1.00x) | 24.413 (2.19x) | 9.140 (0.82x) | 9.801 (0.88x) | 2.19 |

### N = 100,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| createEmpty | 12.884 (1.00x) | 22.033 (1.71x) | 11.041 (0.86x) | 17.069 (1.32x) | 1.71 |
| create1 | 54.379 (1.00x) | 112.571 (2.07x) | 27.666 (0.51x) | 27.857 (0.51x) | 2.07 |
| create2 | 102.269 (1.00x) | 215.319 (2.11x) | 42.933 (0.42x) | 42.874 (0.42x) | 2.11 |
| createFromPrefab | 112.378 (1.00x) | 199.198 (1.77x) | 36.978 (0.33x) | 67.893 (0.60x) | 1.77 |
| destroyEmpty | 54.098 (1.00x) | 68.862 (1.27x) | 29.397 (0.54x) | 68.714 (1.27x) | 1.27 |
| destroy2 | 77.648 (1.00x) | 61.970 (0.80x) | 26.678 (0.34x) | 175.809 (2.26x) | 0.80 |
| addComponent | 94.213 (1.00x) | 127.244 (1.35x) | 41.499 (0.44x) | 142.324 (1.51x) | 1.35 |
| removeComponent | 83.284 (1.00x) | 105.631 (1.27x) | 119.459 (1.43x) | 129.300 (1.55x) | 1.27 |
| get | 10.515 (1.00x) | 41.144 (3.91x) | 11.459 (1.09x) | 22.329 (2.12x) | 3.91 |
| getSet | 9.185 (1.00x) | 40.499 (4.41x) | 12.722 (1.39x) | 27.888 (3.04x) | 4.41 |
| has | 4.602 (1.00x) | 22.510 (4.89x) | 10.028 (2.18x) | 17.922 (3.89x) | 4.89 |
| query1Read | 0.166 (1.00x) | 0.666 (4.01x) | 1.198 (7.22x) | 1.592 (9.60x) | 4.01 |
| query2ReadWrite | 0.262 (1.00x) | 1.701 (6.49x) | 2.363 (9.01x) | 8.410 (32.07x) | 6.49 |
| queryFiltered | 0.166 (1.00x) | 0.675 (4.07x) | 1.172 (7.07x) | 2.946 (17.77x) | 4.07 |
| systemRun | 0.268 (1.00x) | 1.798 (6.72x) | 2.023 (7.56x) | 3.025 (11.31x) | 6.72 |
| queryCreate | 226.801 (1.00x) | 13858.321 (61.10x) | n/a | n/a | 61.10 |
| lookup | 299.072 (1.00x) | 367.909 (1.23x) | n/a | n/a | 1.23 |
| mixedSimulation | 11.372 (1.00x) | 24.280 (2.14x) | 10.318 (0.91x) | 19.294 (1.70x) | 2.14 |

## Tier 2

### N = 1,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 31.227 (1.00x) | 43.097 (1.38x) | n/a | n/a | 1.38 |
| pairIterate | 0.220 (1.00x) | 3.811 (17.32x) | n/a | n/a | 17.32 |
| hierarchyBuild | 373.352 (1.00x) | 449.644 (1.20x) | n/a | n/a | 1.20 |
| hierarchyTraverse | 21.306 (1.00x) | 43.876 (2.06x) | n/a | n/a | 2.06 |
| prefabInheritGet | 14.841 (1.00x) | 23.366 (1.57x) | n/a | n/a | 1.57 |
| observerAdd | 67.778 (1.00x) | 483.752 (7.14x) | n/a | n/a | 7.14 |
| singletonGetSet | 11.631 (1.00x) | 19.371 (1.67x) | n/a | n/a | 1.67 |
| deferAdd | 108.550 (1.00x) | 145.658 (1.34x) | n/a | n/a | 1.34 |
| multiThreadedProgress | 9.811 (1.00x) | 10.882 (1.11x) | n/a | n/a | 1.11 |

### N = 10,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 30.162 (1.00x) | 46.808 (1.55x) | n/a | n/a | 1.55 |
| pairIterate | 0.122 (1.00x) | 1.245 (10.20x) | n/a | n/a | 10.20 |
| hierarchyBuild | 421.521 (1.00x) | 523.135 (1.24x) | n/a | n/a | 1.24 |
| hierarchyTraverse | 28.557 (1.00x) | 56.754 (1.99x) | n/a | n/a | 1.99 |
| prefabInheritGet | 15.385 (1.00x) | 25.033 (1.63x) | n/a | n/a | 1.63 |
| observerAdd | 65.837 (1.00x) | 239.489 (3.64x) | n/a | n/a | 3.64 |
| singletonGetSet | 15.180 (1.00x) | 19.201 (1.26x) | n/a | n/a | 1.26 |
| deferAdd | 114.001 (1.00x) | 221.973 (1.95x) | n/a | n/a | 1.95 |
| multiThreadedProgress | 1.047 (1.00x) | 1.597 (1.53x) | n/a | n/a | 1.53 |

### N = 100,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 31.262 (1.00x) | 82.596 (2.64x) | n/a | n/a | 2.64 |
| pairIterate | 0.112 (1.00x) | 0.954 (8.49x) | n/a | n/a | 8.49 |
| hierarchyBuild | 619.806 (1.00x) | 820.957 (1.32x) | n/a | n/a | 1.32 |
| hierarchyTraverse | 73.269 (1.00x) | 87.983 (1.20x) | n/a | n/a | 1.20 |
| prefabInheritGet | 24.537 (1.00x) | 38.648 (1.58x) | n/a | n/a | 1.58 |
| observerAdd | 67.575 (1.00x) | 205.878 (3.05x) | n/a | n/a | 3.05 |
| singletonGetSet | 11.795 (1.00x) | 19.022 (1.61x) | n/a | n/a | 1.61 |
| deferAdd | 114.205 (1.00x) | 210.269 (1.84x) | n/a | n/a | 1.84 |
| multiThreadedProgress | 0.246 (1.00x) | 0.639 (2.59x) | n/a | n/a | 2.59 |

