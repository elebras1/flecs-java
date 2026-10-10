# Benchmark results

Generated: 2026-10-10 15:32:55  
Git commit: `8f332e9`  
Mode: full

## Machine

| Key | Value |
|---|---|
| os | Linux 7.0.0-28-generic |
| kernel | 7.0.0-28-generic |
| cpu | AMD Ryzen 5 5600GT with Radeon Graphics |
| cores | 12 |
| ram_gb | 15 |
| jvm | GraalVM Community 25.0.4 |
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
| createEmpty | 8.370 (1.00x) | 16.709 (2.00x) | 11.660 (1.39x) | 16.942 (2.02x) | 2.00 |
| create1 | 50.551 (1.00x) | 101.201 (2.00x) | 25.057 (0.50x) | 32.287 (0.64x) | 2.00 |
| create2 | 94.436 (1.00x) | 196.587 (2.08x) | 39.148 (0.41x) | 54.027 (0.57x) | 2.08 |
| createFromPrefab | 106.006 (1.00x) | 192.470 (1.82x) | 35.170 (0.33x) | 73.627 (0.69x) | 1.82 |
| destroyEmpty | 20.620 (1.00x) | 25.174 (1.22x) | 13.547 (0.66x) | 19.889 (0.96x) | 1.22 |
| destroy2 | 23.541 (1.00x) | 28.415 (1.21x) | 22.108 (0.94x) | 28.731 (1.22x) | 1.21 |
| addComponent | 38.880 (1.00x) | 60.281 (1.55x) | 17.174 (0.44x) | 53.872 (1.39x) | 1.55 |
| removeComponent | 36.691 (1.00x) | 54.806 (1.49x) | 32.789 (0.89x) | 57.616 (1.57x) | 1.49 |
| get | 3.456 (1.00x) | 18.075 (5.23x) | 3.873 (1.12x) | 3.197 (0.93x) | 5.23 |
| getSet | 4.955 (1.00x) | 21.680 (4.38x) | 2.604 (0.53x) | 3.471 (0.70x) | 4.38 |
| has | 1.550 (1.00x) | 13.849 (8.94x) | 3.804 (2.45x) | 2.826 (1.82x) | 8.94 |
| query1Read | 0.260 (1.00x) | 0.817 (3.14x) | 0.697 (2.68x) | 0.879 (3.38x) | 3.14 |
| query2ReadWrite | 0.360 (1.00x) | 2.807 (7.80x) | 1.537 (4.27x) | 1.411 (3.92x) | 7.80 |
| queryFiltered | 0.260 (1.00x) | 0.818 (3.15x) | 0.703 (2.70x) | 0.957 (3.68x) | 3.15 |
| systemRun | 0.350 (1.00x) | 2.775 (7.93x) | 1.644 (4.70x) | 17.685 (50.53x) | 7.93 |
| systemRun5 | 0.760 (1.00x) | 2.913 (3.83x) | 4.079 (5.37x) | 19.762 (26.00x) | 3.83 |
| queryCreate | 224.182 (1.00x) | 749.009 (3.34x) | n/a | n/a | 3.34 |
| lookup | 61.636 (1.00x) | 86.555 (1.40x) | n/a | n/a | 1.40 |

### N = 10,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| createEmpty | 11.277 (1.00x) | 15.552 (1.38x) | 13.512 (1.20x) | 17.949 (1.59x) | 1.38 |
| create1 | 56.236 (1.00x) | 98.832 (1.76x) | 26.972 (0.48x) | 37.012 (0.66x) | 1.76 |
| create2 | 102.483 (1.00x) | 180.809 (1.76x) | 41.075 (0.40x) | 56.511 (0.55x) | 1.76 |
| createFromPrefab | 109.179 (1.00x) | 182.693 (1.67x) | 37.282 (0.34x) | 78.101 (0.72x) | 1.67 |
| destroyEmpty | 25.699 (1.00x) | 33.512 (1.30x) | 15.584 (0.61x) | 27.337 (1.06x) | 1.30 |
| destroy2 | 31.828 (1.00x) | 36.016 (1.13x) | 23.022 (0.72x) | 53.715 (1.69x) | 1.13 |
| addComponent | 58.600 (1.00x) | 65.171 (1.11x) | 19.504 (0.33x) | 72.891 (1.24x) | 1.11 |
| removeComponent | 50.118 (1.00x) | 61.127 (1.22x) | 49.614 (0.99x) | 93.339 (1.86x) | 1.22 |
| get | 5.037 (1.00x) | 20.707 (4.11x) | 4.382 (0.87x) | 6.351 (1.26x) | 4.11 |
| getSet | 5.957 (1.00x) | 24.821 (4.17x) | 5.452 (0.92x) | 6.743 (1.13x) | 4.17 |
| has | 1.915 (1.00x) | 14.321 (7.48x) | 5.279 (2.76x) | 5.197 (2.71x) | 7.48 |
| query1Read | 0.174 (1.00x) | 3.685 (21.24x) | 0.973 (5.61x) | 2.153 (12.41x) | 21.24 |
| query2ReadWrite | 0.267 (1.00x) | 5.765 (21.59x) | 1.669 (6.25x) | 4.131 (15.47x) | 21.59 |
| queryFiltered | 0.174 (1.00x) | 3.542 (20.35x) | 0.984 (5.66x) | 2.667 (15.33x) | 20.35 |
| systemRun | 0.270 (1.00x) | 5.716 (21.17x) | 1.716 (6.36x) | 5.459 (20.22x) | 21.17 |
| systemRun5 | 0.706 (1.00x) | 2.741 (3.88x) | 4.400 (6.24x) | 9.169 (13.00x) | 3.88 |
| queryCreate | 224.602 (1.00x) | 769.505 (3.43x) | n/a | n/a | 3.43 |
| lookup | 93.018 (1.00x) | 121.466 (1.31x) | n/a | n/a | 1.31 |

### N = 100,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| createEmpty | 12.510 (1.00x) | 15.701 (1.26x) | 12.729 (1.02x) | 17.642 (1.41x) | 1.26 |
| create1 | 53.905 (1.00x) | 110.665 (2.05x) | 26.359 (0.49x) | 35.507 (0.66x) | 2.05 |
| create2 | 102.681 (1.00x) | 196.537 (1.91x) | 40.845 (0.40x) | 58.610 (0.57x) | 1.91 |
| createFromPrefab | 112.152 (1.00x) | 199.097 (1.78x) | 38.015 (0.34x) | 77.846 (0.69x) | 1.78 |
| destroyEmpty | 45.426 (1.00x) | 53.892 (1.19x) | 22.307 (0.49x) | 57.734 (1.27x) | 1.19 |
| destroy2 | 55.361 (1.00x) | 55.357 (1.00x) | 29.639 (0.54x) | 149.447 (2.70x) | 1.00 |
| addComponent | 81.558 (1.00x) | 105.632 (1.30x) | 41.031 (0.50x) | 123.690 (1.52x) | 1.30 |
| removeComponent | 76.049 (1.00x) | 92.908 (1.22x) | 112.381 (1.48x) | 156.106 (2.05x) | 1.22 |
| get | 9.704 (1.00x) | 37.488 (3.86x) | 10.686 (1.10x) | 17.178 (1.77x) | 3.86 |
| getSet | 9.013 (1.00x) | 36.844 (4.09x) | 12.550 (1.39x) | 20.718 (2.30x) | 4.09 |
| has | 4.637 (1.00x) | 22.653 (4.89x) | 11.563 (2.49x) | 17.477 (3.77x) | 4.89 |
| query1Read | 0.167 (1.00x) | 3.522 (21.16x) | 1.048 (6.30x) | 2.232 (13.41x) | 21.16 |
| query2ReadWrite | 0.272 (1.00x) | 5.737 (21.11x) | 1.797 (6.61x) | 8.318 (30.60x) | 21.11 |
| queryFiltered | 0.171 (1.00x) | 3.524 (20.55x) | 1.066 (6.22x) | 2.814 (16.41x) | 20.55 |
| systemRun | 0.291 (1.00x) | 5.719 (19.64x) | 4.578 (15.72x) | 4.028 (13.83x) | 19.64 |
| systemRun5 | 0.743 (1.00x) | 2.700 (3.63x) | 8.574 (11.53x) | 13.490 (18.15x) | 3.63 |
| queryCreate | 228.275 (1.00x) | 774.435 (3.39x) | n/a | n/a | 3.39 |
| lookup | 253.951 (1.00x) | 316.440 (1.25x) | n/a | n/a | 1.25 |

## Tier 2

### N = 1,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 32.376 (1.00x) | 52.092 (1.61x) | n/a | n/a | 1.61 |
| pairIterate | 0.210 (1.00x) | 2.846 (13.55x) | n/a | n/a | 13.55 |
| hierarchyBuild | 368.403 (1.00x) | 401.251 (1.09x) | n/a | n/a | 1.09 |
| hierarchyTraverse | 21.950 (1.00x) | 28.736 (1.31x) | n/a | n/a | 1.31 |
| prefabInheritGet | 14.845 (1.00x) | 24.854 (1.67x) | n/a | n/a | 1.67 |
| observerAdd | 67.426 (1.00x) | 402.176 (5.96x) | n/a | n/a | 5.96 |
| singletonGetSet | 12.260 (1.00x) | 18.407 (1.50x) | n/a | n/a | 1.50 |
| deferAdd | 108.561 (1.00x) | 141.624 (1.30x) | n/a | n/a | 1.30 |
| multiThreadedProgress | 9.655 (1.00x) | 10.343 (1.07x) | n/a | n/a | 1.07 |
| setValue | 47.186 (1.00x) | 84.438 (1.79x) | n/a | n/a | 1.79 |
| bulkCreate | 5.510 (1.00x) | 11.692 (2.12x) | n/a | n/a | 2.12 |

### N = 10,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 31.877 (1.00x) | 49.853 (1.56x) | n/a | n/a | 1.56 |
| pairIterate | 0.121 (1.00x) | 2.678 (22.14x) | n/a | n/a | 22.14 |
| hierarchyBuild | 412.618 (1.00x) | 421.462 (1.02x) | n/a | n/a | 1.02 |
| hierarchyTraverse | 26.843 (1.00x) | 37.680 (1.40x) | n/a | n/a | 1.40 |
| prefabInheritGet | 15.244 (1.00x) | 25.679 (1.68x) | n/a | n/a | 1.68 |
| observerAdd | 66.481 (1.00x) | 227.083 (3.42x) | n/a | n/a | 3.42 |
| singletonGetSet | 13.426 (1.00x) | 18.326 (1.36x) | n/a | n/a | 1.36 |
| deferAdd | 115.466 (1.00x) | 151.369 (1.31x) | n/a | n/a | 1.31 |
| multiThreadedProgress | 1.046 (1.00x) | 2.737 (2.62x) | n/a | n/a | 2.62 |
| setValue | 50.561 (1.00x) | 84.100 (1.66x) | n/a | n/a | 1.66 |
| bulkCreate | 2.532 (1.00x) | 4.407 (1.74x) | n/a | n/a | 1.74 |

### N = 100,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 31.438 (1.00x) | 63.083 (2.01x) | n/a | n/a | 2.01 |
| pairIterate | 0.121 (1.00x) | 2.679 (22.09x) | n/a | n/a | 22.09 |
| hierarchyBuild | 529.753 (1.00x) | 556.825 (1.05x) | n/a | n/a | 1.05 |
| hierarchyTraverse | 56.491 (1.00x) | 61.021 (1.08x) | n/a | n/a | 1.08 |
| prefabInheritGet | 24.157 (1.00x) | 33.817 (1.40x) | n/a | n/a | 1.40 |
| observerAdd | 66.399 (1.00x) | 191.964 (2.89x) | n/a | n/a | 2.89 |
| singletonGetSet | 12.359 (1.00x) | 18.374 (1.49x) | n/a | n/a | 1.49 |
| deferAdd | 114.536 (1.00x) | 196.655 (1.72x) | n/a | n/a | 1.72 |
| multiThreadedProgress | 0.202 (1.00x) | 1.663 (8.24x) | n/a | n/a | 8.24 |
| setValue | 69.019 (1.00x) | 98.228 (1.42x) | n/a | n/a | 1.42 |
| bulkCreate | 2.108 (1.00x) | 3.810 (1.81x) | n/a | n/a | 1.81 |

