# Benchmark results

Generated: 2026-10-03 22:49:35  
Git commit: `a26f8f7`  
Mode: full

## Machine

| Key | Value |
|---|---|
| os | Linux 7.0.0-28-generic |
| kernel | 7.0.0-28-generic |
| cpu | AMD Ryzen 5 5600GT with Radeon Graphics |
| cores | 12 |
| ram_gb | 15 |
| jvm | BellSoft 27 |
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
| createEmpty | 8.520 (1.00x) | 15.835 (1.86x) | 13.302 (1.56x) | 21.457 (2.52x) | 1.86 |
| create1 | 51.892 (1.00x) | 104.194 (2.01x) | 27.359 (0.53x) | 33.104 (0.64x) | 2.01 |
| create2 | 99.998 (1.00x) | 199.706 (2.00x) | 37.782 (0.38x) | 49.814 (0.50x) | 2.00 |
| createFromPrefab | 106.973 (1.00x) | 186.309 (1.74x) | 33.330 (0.31x) | 71.216 (0.67x) | 1.74 |
| destroyEmpty | 19.761 (1.00x) | 25.401 (1.29x) | 13.038 (0.66x) | 21.860 (1.11x) | 1.29 |
| destroy2 | 23.361 (1.00x) | 28.869 (1.24x) | 22.061 (0.94x) | 27.865 (1.19x) | 1.24 |
| addComponent | 36.686 (1.00x) | 60.643 (1.65x) | 16.985 (0.46x) | 53.211 (1.45x) | 1.65 |
| removeComponent | 33.466 (1.00x) | 55.487 (1.66x) | 36.024 (1.08x) | 50.606 (1.51x) | 1.66 |
| get | 3.490 (1.00x) | 17.900 (5.13x) | 2.864 (0.82x) | 2.902 (0.83x) | 5.13 |
| getSet | 5.415 (1.00x) | 22.265 (4.11x) | 3.361 (0.62x) | 3.376 (0.62x) | 4.11 |
| has | 1.510 (1.00x) | 13.777 (9.12x) | 1.832 (1.21x) | 4.336 (2.87x) | 9.12 |
| query1Read | 0.260 (1.00x) | 0.785 (3.02x) | 0.691 (2.66x) | 1.411 (5.43x) | 3.02 |
| query2ReadWrite | 0.360 (1.00x) | 0.866 (2.41x) | 1.970 (5.47x) | 3.430 (9.53x) | 2.41 |
| queryFiltered | 0.260 (1.00x) | 0.781 (3.00x) | 0.695 (2.67x) | 2.074 (7.98x) | 3.00 |
| systemRun | 0.350 (1.00x) | 3.419 (9.77x) | 2.711 (7.75x) | 18.927 (54.08x) | 9.77 |
| queryCreate | 221.506 (1.00x) | 1046.905 (4.73x) | n/a | n/a | 4.73 |
| lookup | 61.581 (1.00x) | 105.061 (1.71x) | n/a | n/a | 1.71 |
| mixedSimulation | 11.850 (1.00x) | 22.723 (1.92x) | 10.398 (0.88x) | 11.716 (0.99x) | 1.92 |

### N = 10,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| createEmpty | 11.641 (1.00x) | 14.937 (1.28x) | 13.528 (1.16x) | 19.326 (1.66x) | 1.28 |
| create1 | 58.943 (1.00x) | 102.220 (1.73x) | 27.345 (0.46x) | 33.416 (0.57x) | 1.73 |
| create2 | 105.401 (1.00x) | 191.388 (1.82x) | 39.614 (0.38x) | 66.083 (0.63x) | 1.82 |
| createFromPrefab | 118.041 (1.00x) | 188.067 (1.59x) | 35.847 (0.30x) | 75.942 (0.64x) | 1.59 |
| destroyEmpty | 26.896 (1.00x) | 31.152 (1.16x) | 16.173 (0.60x) | 32.464 (1.21x) | 1.16 |
| destroy2 | 31.884 (1.00x) | 35.810 (1.12x) | 24.205 (0.76x) | 43.539 (1.37x) | 1.12 |
| addComponent | 59.147 (1.00x) | 67.074 (1.13x) | 17.587 (0.30x) | 65.761 (1.11x) | 1.13 |
| removeComponent | 53.818 (1.00x) | 62.773 (1.17x) | 47.575 (0.88x) | 71.585 (1.33x) | 1.17 |
| get | 4.705 (1.00x) | 21.843 (4.64x) | 3.656 (0.78x) | 5.787 (1.23x) | 4.64 |
| getSet | 5.795 (1.00x) | 26.267 (4.53x) | 4.668 (0.81x) | 6.461 (1.11x) | 4.53 |
| has | 1.904 (1.00x) | 13.881 (7.29x) | 3.014 (1.58x) | 5.319 (2.79x) | 7.29 |
| query1Read | 0.174 (1.00x) | 0.678 (3.90x) | 0.921 (5.29x) | 1.864 (10.71x) | 3.90 |
| query2ReadWrite | 0.269 (1.00x) | 0.696 (2.59x) | 2.084 (7.75x) | 3.476 (12.92x) | 2.59 |
| queryFiltered | 0.174 (1.00x) | 0.680 (3.90x) | 0.945 (5.43x) | 2.600 (14.94x) | 3.90 |
| systemRun | 0.270 (1.00x) | 2.885 (10.69x) | 2.825 (10.46x) | 4.781 (17.71x) | 10.69 |
| queryCreate | 221.063 (1.00x) | 1073.804 (4.86x) | n/a | n/a | 4.86 |
| lookup | 90.136 (1.00x) | 135.300 (1.50x) | n/a | n/a | 1.50 |
| mixedSimulation | 11.669 (1.00x) | 22.646 (1.94x) | 9.956 (0.85x) | 12.128 (1.04x) | 1.94 |

### N = 100,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| createEmpty | 12.255 (1.00x) | 14.769 (1.21x) | 12.922 (1.05x) | 19.540 (1.59x) | 1.21 |
| create1 | 55.095 (1.00x) | 99.930 (1.81x) | 26.899 (0.49x) | 33.965 (0.62x) | 1.81 |
| create2 | 103.217 (1.00x) | 189.959 (1.84x) | 39.124 (0.38x) | 53.464 (0.52x) | 1.84 |
| createFromPrefab | 110.787 (1.00x) | 179.415 (1.62x) | 34.884 (0.31x) | 77.299 (0.70x) | 1.62 |
| destroyEmpty | 44.314 (1.00x) | 57.762 (1.30x) | 21.668 (0.49x) | 60.663 (1.37x) | 1.30 |
| destroy2 | 51.885 (1.00x) | 56.672 (1.09x) | 27.572 (0.53x) | 123.944 (2.39x) | 1.09 |
| addComponent | 77.897 (1.00x) | 92.269 (1.18x) | 34.299 (0.44x) | 94.238 (1.21x) | 1.18 |
| removeComponent | 70.430 (1.00x) | 84.157 (1.19x) | 118.481 (1.68x) | 93.339 (1.33x) | 1.19 |
| get | 9.471 (1.00x) | 39.084 (4.13x) | 9.826 (1.04x) | 16.304 (1.72x) | 4.13 |
| getSet | 8.974 (1.00x) | 40.186 (4.48x) | 11.744 (1.31x) | 21.489 (2.39x) | 4.48 |
| has | 4.717 (1.00x) | 19.960 (4.23x) | 8.572 (1.82x) | 17.154 (3.64x) | 4.23 |
| query1Read | 0.186 (1.00x) | 0.662 (3.57x) | 1.018 (5.49x) | 2.118 (11.42x) | 3.57 |
| query2ReadWrite | 0.270 (1.00x) | 0.685 (2.53x) | 2.177 (8.05x) | 7.569 (28.00x) | 2.53 |
| queryFiltered | 0.166 (1.00x) | 0.663 (4.00x) | 1.064 (6.41x) | 2.629 (15.85x) | 4.00 |
| systemRun | 0.266 (1.00x) | 2.860 (10.73x) | 2.795 (10.49x) | 6.874 (25.80x) | 10.73 |
| queryCreate | 220.623 (1.00x) | 998.710 (4.53x) | n/a | n/a | 4.53 |
| lookup | 205.346 (1.00x) | 305.472 (1.49x) | n/a | n/a | 1.49 |
| mixedSimulation | 11.706 (1.00x) | 22.250 (1.90x) | 9.917 (0.85x) | 18.661 (1.59x) | 1.90 |

## Tier 2

### N = 1,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 32.001 (1.00x) | 44.628 (1.39x) | n/a | n/a | 1.39 |
| pairIterate | 0.210 (1.00x) | 3.797 (18.08x) | n/a | n/a | 18.08 |
| hierarchyBuild | 372.310 (1.00x) | 396.400 (1.06x) | n/a | n/a | 1.06 |
| hierarchyTraverse | 20.705 (1.00x) | 40.605 (1.96x) | n/a | n/a | 1.96 |
| prefabInheritGet | 14.870 (1.00x) | 24.478 (1.65x) | n/a | n/a | 1.65 |
| observerAdd | 68.167 (1.00x) | 379.415 (5.57x) | n/a | n/a | 5.57 |
| singletonGetSet | 12.071 (1.00x) | 18.173 (1.51x) | n/a | n/a | 1.51 |
| deferAdd | 161.129 (1.00x) | 145.658 (0.90x) | n/a | n/a | 0.90 |
| multiThreadedProgress | 9.585 (1.00x) | 10.353 (1.08x) | n/a | n/a | 1.08 |

### N = 10,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 31.017 (1.00x) | 47.222 (1.52x) | n/a | n/a | 1.52 |
| pairIterate | 0.121 (1.00x) | 1.024 (8.46x) | n/a | n/a | 8.46 |
| hierarchyBuild | 382.481 (1.00x) | 468.544 (1.23x) | n/a | n/a | 1.23 |
| hierarchyTraverse | 27.019 (1.00x) | 44.253 (1.64x) | n/a | n/a | 1.64 |
| prefabInheritGet | 15.240 (1.00x) | 24.702 (1.62x) | n/a | n/a | 1.62 |
| observerAdd | 73.552 (1.00x) | 222.951 (3.03x) | n/a | n/a | 3.03 |
| singletonGetSet | 11.992 (1.00x) | 18.130 (1.51x) | n/a | n/a | 1.51 |
| deferAdd | 113.370 (1.00x) | 153.959 (1.36x) | n/a | n/a | 1.36 |
| multiThreadedProgress | 1.043 (1.00x) | 1.681 (1.61x) | n/a | n/a | 1.61 |

### N = 100,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 31.125 (1.00x) | 68.641 (2.21x) | n/a | n/a | 2.21 |
| pairIterate | 0.117 (1.00x) | 0.829 (7.08x) | n/a | n/a | 7.08 |
| hierarchyBuild | 496.801 (1.00x) | 693.132 (1.40x) | n/a | n/a | 1.40 |
| hierarchyTraverse | 52.661 (1.00x) | 77.098 (1.46x) | n/a | n/a | 1.46 |
| prefabInheritGet | 23.678 (1.00x) | 31.964 (1.35x) | n/a | n/a | 1.35 |
| observerAdd | 66.587 (1.00x) | 168.594 (2.53x) | n/a | n/a | 2.53 |
| singletonGetSet | 12.053 (1.00x) | 18.077 (1.50x) | n/a | n/a | 1.50 |
| deferAdd | 110.092 (1.00x) | 188.251 (1.71x) | n/a | n/a | 1.71 |
| multiThreadedProgress | 0.203 (1.00x) | 0.846 (4.17x) | n/a | n/a | 4.17 |

