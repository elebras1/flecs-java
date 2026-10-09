# Benchmark results

Generated: 2026-10-09 20:57:02  
Git commit: `1e37880`  
Mode: full

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
| flecs-java | 1.0.0 |
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
| createEmpty | 8.820 (1.00x) | 17.025 (1.93x) | 9.733 (1.10x) | 17.629 (2.00x) | 1.93 |
| create1 | 54.370 (1.00x) | 100.377 (1.85x) | 25.603 (0.47x) | 27.495 (0.51x) | 1.85 |
| create2 | 110.415 (1.00x) | 180.299 (1.63x) | 39.796 (0.36x) | 38.237 (0.35x) | 1.63 |
| createFromPrefab | 108.035 (1.00x) | 197.573 (1.83x) | 33.866 (0.31x) | 62.286 (0.58x) | 1.83 |
| destroyEmpty | 20.035 (1.00x) | 25.681 (1.28x) | 12.237 (0.61x) | 18.526 (0.92x) | 1.28 |
| destroy2 | 23.760 (1.00x) | 29.723 (1.25x) | 18.968 (0.80x) | 25.744 (1.08x) | 1.25 |
| addComponent | 38.425 (1.00x) | 62.921 (1.64x) | 17.800 (0.46x) | 52.550 (1.37x) | 1.64 |
| removeComponent | 34.550 (1.00x) | 54.813 (1.59x) | 31.226 (0.90x) | 53.021 (1.53x) | 1.59 |
| get | 4.570 (1.00x) | 17.892 (3.92x) | 3.356 (0.73x) | 3.230 (0.71x) | 3.92 |
| getSet | 5.665 (1.00x) | 21.863 (3.86x) | 3.748 (0.66x) | 3.493 (0.62x) | 3.86 |
| has | 1.510 (1.00x) | 13.674 (9.06x) | 3.239 (2.14x) | 2.814 (1.86x) | 9.06 |
| query1Read | 0.260 (1.00x) | 0.815 (3.14x) | 0.702 (2.70x) | 0.728 (2.80x) | 3.14 |
| query2ReadWrite | 0.360 (1.00x) | 1.233 (3.43x) | 1.664 (4.62x) | 1.860 (5.17x) | 3.43 |
| queryFiltered | 0.260 (1.00x) | 0.817 (3.14x) | 0.699 (2.69x) | 0.875 (3.37x) | 3.14 |
| systemRun | 0.540 (1.00x) | 1.555 (2.88x) | 1.909 (3.53x) | 18.457 (34.18x) | 2.88 |
| queryCreate | 220.926 (1.00x) | 752.222 (3.40x) | n/a | n/a | 3.40 |
| lookup | 57.111 (1.00x) | 88.931 (1.56x) | n/a | n/a | 1.56 |
| mixedSimulation | 12.135 (1.00x) | 22.172 (1.83x) | 9.230 (0.76x) | 9.125 (0.75x) | 1.83 |

### N = 10,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| createEmpty | 11.862 (1.00x) | 15.839 (1.34x) | 12.402 (1.05x) | 17.860 (1.51x) | 1.34 |
| create1 | 60.886 (1.00x) | 97.451 (1.60x) | 27.088 (0.44x) | 27.609 (0.45x) | 1.60 |
| create2 | 112.286 (1.00x) | 174.379 (1.55x) | 41.102 (0.37x) | 40.023 (0.36x) | 1.55 |
| createFromPrefab | 110.540 (1.00x) | 196.572 (1.78x) | 35.846 (0.32x) | 63.785 (0.58x) | 1.78 |
| destroyEmpty | 25.732 (1.00x) | 33.483 (1.30x) | 14.173 (0.55x) | 32.152 (1.25x) | 1.30 |
| destroy2 | 32.274 (1.00x) | 36.570 (1.13x) | 20.750 (0.64x) | 51.924 (1.61x) | 1.13 |
| addComponent | 59.946 (1.00x) | 68.474 (1.14x) | 18.828 (0.31x) | 63.976 (1.07x) | 1.14 |
| removeComponent | 53.071 (1.00x) | 63.665 (1.20x) | 43.426 (0.82x) | 70.441 (1.33x) | 1.20 |
| get | 4.796 (1.00x) | 20.038 (4.18x) | 5.245 (1.09x) | 6.375 (1.33x) | 4.18 |
| getSet | 5.734 (1.00x) | 24.938 (4.35x) | 6.529 (1.14x) | 6.773 (1.18x) | 4.35 |
| has | 1.932 (1.00x) | 14.316 (7.41x) | 5.106 (2.64x) | 5.167 (2.67x) | 7.41 |
| query1Read | 0.173 (1.00x) | 0.697 (4.03x) | 1.046 (6.05x) | 1.303 (7.53x) | 4.03 |
| query2ReadWrite | 0.268 (1.00x) | 1.070 (3.99x) | 2.002 (7.47x) | 2.922 (10.90x) | 3.99 |
| queryFiltered | 0.174 (1.00x) | 0.688 (3.96x) | 1.086 (6.24x) | 1.907 (10.96x) | 3.96 |
| systemRun | 0.270 (1.00x) | 1.282 (4.75x) | 2.117 (7.84x) | 4.123 (15.27x) | 4.75 |
| queryCreate | 222.337 (1.00x) | 713.739 (3.21x) | n/a | n/a | 3.21 |
| lookup | 89.527 (1.00x) | 132.164 (1.48x) | n/a | n/a | 1.48 |
| mixedSimulation | 12.107 (1.00x) | 22.235 (1.84x) | 8.490 (0.70x) | 9.434 (0.78x) | 1.84 |

### N = 100,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| createEmpty | 13.131 (1.00x) | 16.151 (1.23x) | 11.099 (0.85x) | 17.641 (1.34x) | 1.23 |
| create1 | 58.714 (1.00x) | 112.320 (1.91x) | 26.857 (0.46x) | 28.053 (0.48x) | 1.91 |
| create2 | 109.546 (1.00x) | 195.638 (1.79x) | 40.553 (0.37x) | 40.948 (0.37x) | 1.79 |
| createFromPrefab | 114.423 (1.00x) | 216.870 (1.90x) | 36.207 (0.32x) | 63.851 (0.56x) | 1.90 |
| destroyEmpty | 51.837 (1.00x) | 58.438 (1.13x) | 20.219 (0.39x) | 65.455 (1.26x) | 1.13 |
| destroy2 | 78.796 (1.00x) | 62.611 (0.79x) | 27.773 (0.35x) | 161.267 (2.05x) | 0.79 |
| addComponent | 100.622 (1.00x) | 122.469 (1.22x) | 30.311 (0.30x) | 105.872 (1.05x) | 1.22 |
| removeComponent | 98.083 (1.00x) | 108.278 (1.10x) | 123.258 (1.26x) | 122.861 (1.25x) | 1.10 |
| get | 9.516 (1.00x) | 39.041 (4.10x) | 10.659 (1.12x) | 22.875 (2.40x) | 4.10 |
| getSet | 8.979 (1.00x) | 36.235 (4.04x) | 12.405 (1.38x) | 16.596 (1.85x) | 4.04 |
| has | 4.738 (1.00x) | 22.747 (4.80x) | 9.114 (1.92x) | 12.376 (2.61x) | 4.80 |
| query1Read | 0.176 (1.00x) | 0.671 (3.82x) | 1.157 (6.58x) | 1.824 (10.37x) | 3.82 |
| query2ReadWrite | 0.306 (1.00x) | 1.126 (3.68x) | 2.203 (7.21x) | 5.987 (19.59x) | 3.68 |
| queryFiltered | 0.173 (1.00x) | 0.670 (3.88x) | 1.175 (6.80x) | 2.291 (13.26x) | 3.88 |
| systemRun | 0.284 (1.00x) | 1.285 (4.52x) | 2.170 (7.64x) | 2.752 (9.69x) | 4.52 |
| queryCreate | 225.845 (1.00x) | 687.391 (3.04x) | n/a | n/a | 3.04 |
| lookup | 297.764 (1.00x) | 376.020 (1.26x) | n/a | n/a | 1.26 |
| mixedSimulation | 12.057 (1.00x) | 21.788 (1.81x) | 9.380 (0.78x) | 16.790 (1.39x) | 1.81 |

## Tier 2

### N = 1,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 33.100 (1.00x) | 46.606 (1.41x) | n/a | n/a | 1.41 |
| pairIterate | 0.305 (1.00x) | 3.968 (13.01x) | n/a | n/a | 13.01 |
| hierarchyBuild | 409.261 (1.00x) | 415.061 (1.01x) | n/a | n/a | 1.01 |
| hierarchyTraverse | 21.785 (1.00x) | 39.531 (1.81x) | n/a | n/a | 1.81 |
| prefabInheritGet | 14.860 (1.00x) | 21.953 (1.48x) | n/a | n/a | 1.48 |
| observerAdd | 68.645 (1.00x) | 422.718 (6.16x) | n/a | n/a | 6.16 |
| singletonGetSet | 11.640 (1.00x) | 18.847 (1.62x) | n/a | n/a | 1.62 |
| deferAdd | 159.890 (1.00x) | 144.271 (0.90x) | n/a | n/a | 0.90 |
| multiThreadedProgress | 9.600 (1.00x) | 10.833 (1.13x) | n/a | n/a | 1.13 |

### N = 10,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 31.414 (1.00x) | 51.946 (1.65x) | n/a | n/a | 1.65 |
| pairIterate | 0.122 (1.00x) | 0.955 (7.82x) | n/a | n/a | 7.82 |
| hierarchyBuild | 452.226 (1.00x) | 513.102 (1.13x) | n/a | n/a | 1.13 |
| hierarchyTraverse | 27.461 (1.00x) | 50.883 (1.85x) | n/a | n/a | 1.85 |
| prefabInheritGet | 15.526 (1.00x) | 22.601 (1.46x) | n/a | n/a | 1.46 |
| observerAdd | 73.445 (1.00x) | 236.317 (3.22x) | n/a | n/a | 3.22 |
| singletonGetSet | 11.905 (1.00x) | 17.843 (1.50x) | n/a | n/a | 1.50 |
| deferAdd | 114.646 (1.00x) | 156.115 (1.36x) | n/a | n/a | 1.36 |
| multiThreadedProgress | 1.044 (1.00x) | 1.480 (1.42x) | n/a | n/a | 1.42 |

### N = 100,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 31.567 (1.00x) | 97.275 (3.08x) | n/a | n/a | 3.08 |
| pairIterate | 0.113 (1.00x) | 0.772 (6.86x) | n/a | n/a | 6.86 |
| hierarchyBuild | 583.675 (1.00x) | 814.740 (1.40x) | n/a | n/a | 1.40 |
| hierarchyTraverse | 72.227 (1.00x) | 76.608 (1.06x) | n/a | n/a | 1.06 |
| prefabInheritGet | 23.769 (1.00x) | 31.117 (1.31x) | n/a | n/a | 1.31 |
| observerAdd | 68.565 (1.00x) | 205.573 (3.00x) | n/a | n/a | 3.00 |
| singletonGetSet | 12.186 (1.00x) | 18.262 (1.50x) | n/a | n/a | 1.50 |
| deferAdd | 115.589 (1.00x) | 216.089 (1.87x) | n/a | n/a | 1.87 |
| multiThreadedProgress | 0.290 (1.00x) | 0.496 (1.71x) | n/a | n/a | 1.71 |

