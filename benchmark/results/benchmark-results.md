# Benchmark results

Generated: 2026-10-10 10:52:16  
Git commit: `a4bcb45`  
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
| createEmpty | 8.455 (1.00x) | 17.245 (2.04x) | 9.752 (1.15x) | 17.717 (2.10x) | 2.04 |
| create1 | 51.648 (1.00x) | 102.138 (1.98x) | 25.327 (0.49x) | 32.174 (0.62x) | 1.98 |
| create2 | 96.071 (1.00x) | 189.242 (1.97x) | 39.363 (0.41x) | 38.268 (0.40x) | 1.97 |
| createFromPrefab | 107.067 (1.00x) | 194.922 (1.82x) | 33.676 (0.31x) | 62.722 (0.59x) | 1.82 |
| destroyEmpty | 20.217 (1.00x) | 25.993 (1.29x) | 12.249 (0.61x) | 18.508 (0.92x) | 1.29 |
| destroy2 | 23.562 (1.00x) | 29.315 (1.24x) | 18.945 (0.80x) | 24.940 (1.06x) | 1.24 |
| addComponent | 37.202 (1.00x) | 60.882 (1.64x) | 18.146 (0.49x) | 51.701 (1.39x) | 1.64 |
| removeComponent | 34.577 (1.00x) | 55.693 (1.61x) | 32.911 (0.95x) | 51.550 (1.49x) | 1.61 |
| get | 3.466 (1.00x) | 17.893 (5.16x) | 2.864 (0.83x) | 3.247 (0.94x) | 5.16 |
| getSet | 5.066 (1.00x) | 22.247 (4.39x) | 1.993 (0.39x) | 3.601 (0.71x) | 4.39 |
| has | 1.510 (1.00x) | 14.143 (9.37x) | 1.465 (0.97x) | 2.821 (1.87x) | 9.37 |
| query1Read | 0.270 (1.00x) | 0.801 (2.97x) | 0.708 (2.62x) | 0.718 (2.66x) | 2.97 |
| query2ReadWrite | 0.360 (1.00x) | 1.276 (3.54x) | 1.648 (4.58x) | 1.889 (5.25x) | 3.54 |
| queryFiltered | 0.260 (1.00x) | 0.812 (3.12x) | 0.722 (2.78x) | 0.864 (3.32x) | 3.12 |
| systemRun | 0.350 (1.00x) | 1.540 (4.40x) | 1.891 (5.40x) | 18.065 (51.61x) | 4.40 |
| systemRun5 | 0.760 (1.00x) | 2.795 (3.68x) | 3.852 (5.07x) | 20.695 (27.23x) | 3.68 |
| queryCreate | 235.609 (1.00x) | 755.300 (3.21x) | n/a | n/a | 3.21 |
| lookup | 60.614 (1.00x) | 91.133 (1.50x) | n/a | n/a | 1.50 |

### N = 10,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| createEmpty | 11.476 (1.00x) | 19.591 (1.71x) | 12.208 (1.06x) | 18.196 (1.59x) | 1.71 |
| create1 | 55.821 (1.00x) | 101.012 (1.81x) | 26.892 (0.48x) | 27.494 (0.49x) | 1.81 |
| create2 | 99.365 (1.00x) | 185.076 (1.86x) | 41.077 (0.41x) | 40.240 (0.40x) | 1.86 |
| createFromPrefab | 114.314 (1.00x) | 201.508 (1.76x) | 36.965 (0.32x) | 63.399 (0.55x) | 1.76 |
| destroyEmpty | 25.758 (1.00x) | 33.690 (1.31x) | 14.127 (0.55x) | 28.980 (1.13x) | 1.31 |
| destroy2 | 34.509 (1.00x) | 36.282 (1.05x) | 20.926 (0.61x) | 49.858 (1.44x) | 1.05 |
| addComponent | 62.802 (1.00x) | 70.691 (1.13x) | 20.012 (0.32x) | 65.727 (1.05x) | 1.13 |
| removeComponent | 56.677 (1.00x) | 63.659 (1.12x) | 40.928 (0.72x) | 65.496 (1.16x) | 1.12 |
| get | 4.871 (1.00x) | 21.502 (4.41x) | 5.570 (1.14x) | 6.562 (1.35x) | 4.41 |
| getSet | 6.210 (1.00x) | 25.510 (4.11x) | 6.635 (1.07x) | 6.056 (0.98x) | 4.11 |
| has | 2.107 (1.00x) | 14.907 (7.07x) | 5.164 (2.45x) | 5.681 (2.70x) | 7.07 |
| query1Read | 0.174 (1.00x) | 0.689 (3.96x) | 0.990 (5.69x) | 1.295 (7.44x) | 3.96 |
| query2ReadWrite | 0.267 (1.00x) | 1.105 (4.14x) | 1.974 (7.39x) | 3.041 (11.38x) | 4.14 |
| queryFiltered | 0.174 (1.00x) | 0.691 (3.97x) | 0.975 (5.60x) | 1.876 (10.78x) | 3.97 |
| systemRun | 0.270 (1.00x) | 1.288 (4.77x) | 2.175 (8.06x) | 4.243 (15.71x) | 4.77 |
| systemRun5 | 0.706 (1.00x) | 2.601 (3.68x) | 4.519 (6.40x) | 7.390 (10.47x) | 3.68 |
| queryCreate | 229.656 (1.00x) | 754.545 (3.29x) | n/a | n/a | 3.29 |
| lookup | 92.361 (1.00x) | 131.063 (1.42x) | n/a | n/a | 1.42 |

### N = 100,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| createEmpty | 12.557 (1.00x) | 16.184 (1.29x) | 11.002 (0.88x) | 17.651 (1.41x) | 1.29 |
| create1 | 55.137 (1.00x) | 115.694 (2.10x) | 26.294 (0.48x) | 28.439 (0.52x) | 2.10 |
| create2 | 102.536 (1.00x) | 197.102 (1.92x) | 40.635 (0.40x) | 41.316 (0.40x) | 1.92 |
| createFromPrefab | 118.330 (1.00x) | 210.921 (1.78x) | 36.644 (0.31x) | 64.153 (0.54x) | 1.78 |
| destroyEmpty | 59.691 (1.00x) | 62.450 (1.05x) | 17.272 (0.29x) | 55.542 (0.93x) | 1.05 |
| destroy2 | 84.314 (1.00x) | 64.476 (0.76x) | 31.801 (0.38x) | 143.455 (1.70x) | 0.76 |
| addComponent | 107.980 (1.00x) | 136.447 (1.26x) | 66.424 (0.62x) | 115.740 (1.07x) | 1.26 |
| removeComponent | 132.287 (1.00x) | 153.471 (1.16x) | 127.837 (0.97x) | 118.613 (0.90x) | 1.16 |
| get | 9.613 (1.00x) | 38.653 (4.02x) | 10.656 (1.11x) | 18.610 (1.94x) | 4.02 |
| getSet | 9.541 (1.00x) | 41.716 (4.37x) | 12.990 (1.36x) | 13.140 (1.38x) | 4.37 |
| has | 4.758 (1.00x) | 23.277 (4.89x) | 9.292 (1.95x) | 14.862 (3.12x) | 4.89 |
| query1Read | 0.166 (1.00x) | 0.674 (4.06x) | 1.139 (6.85x) | 1.638 (9.86x) | 4.06 |
| query2ReadWrite | 0.265 (1.00x) | 1.120 (4.23x) | 2.363 (8.92x) | 6.158 (23.25x) | 4.23 |
| queryFiltered | 0.170 (1.00x) | 0.670 (3.93x) | 1.188 (6.98x) | 2.617 (15.37x) | 3.93 |
| systemRun | 0.264 (1.00x) | 1.283 (4.86x) | 2.256 (8.55x) | 2.836 (10.75x) | 4.86 |
| systemRun5 | 0.692 (1.00x) | 2.590 (3.74x) | 9.044 (13.06x) | 12.341 (17.82x) | 3.74 |
| queryCreate | 229.539 (1.00x) | 769.675 (3.35x) | n/a | n/a | 3.35 |
| lookup | 314.683 (1.00x) | 398.101 (1.27x) | n/a | n/a | 1.27 |

## Tier 2

### N = 1,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 31.502 (1.00x) | 46.869 (1.49x) | n/a | n/a | 1.49 |
| pairIterate | 0.220 (1.00x) | 4.056 (18.44x) | n/a | n/a | 18.44 |
| hierarchyBuild | 380.418 (1.00x) | 439.713 (1.16x) | n/a | n/a | 1.16 |
| hierarchyTraverse | 21.087 (1.00x) | 39.052 (1.85x) | n/a | n/a | 1.85 |
| prefabInheritGet | 15.206 (1.00x) | 22.254 (1.46x) | n/a | n/a | 1.46 |
| observerAdd | 67.329 (1.00x) | 433.642 (6.44x) | n/a | n/a | 6.44 |
| singletonGetSet | 11.901 (1.00x) | 18.732 (1.57x) | n/a | n/a | 1.57 |
| deferAdd | 106.822 (1.00x) | 145.605 (1.36x) | n/a | n/a | 1.36 |
| multiThreadedProgress | 9.640 (1.00x) | 10.448 (1.08x) | n/a | n/a | 1.08 |
| setValue | 45.418 (1.00x) | 85.060 (1.87x) | n/a | n/a | 1.87 |
| bulkCreate | 5.591 (1.00x) | 12.748 (2.28x) | n/a | n/a | 2.28 |

### N = 10,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 31.109 (1.00x) | 51.269 (1.65x) | n/a | n/a | 1.65 |
| pairIterate | 0.130 (1.00x) | 0.935 (7.22x) | n/a | n/a | 7.22 |
| hierarchyBuild | 419.056 (1.00x) | 509.081 (1.21x) | n/a | n/a | 1.21 |
| hierarchyTraverse | 26.468 (1.00x) | 52.312 (1.98x) | n/a | n/a | 1.98 |
| prefabInheritGet | 15.233 (1.00x) | 22.137 (1.45x) | n/a | n/a | 1.45 |
| observerAdd | 66.633 (1.00x) | 238.332 (3.58x) | n/a | n/a | 3.58 |
| singletonGetSet | 12.863 (1.00x) | 18.097 (1.41x) | n/a | n/a | 1.41 |
| deferAdd | 114.352 (1.00x) | 168.209 (1.47x) | n/a | n/a | 1.47 |
| multiThreadedProgress | 1.045 (1.00x) | 1.437 (1.38x) | n/a | n/a | 1.38 |
| setValue | 50.727 (1.00x) | 88.664 (1.75x) | n/a | n/a | 1.75 |
| bulkCreate | 2.616 (1.00x) | 4.925 (1.88x) | n/a | n/a | 1.88 |

### N = 100,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 31.156 (1.00x) | 98.932 (3.18x) | n/a | n/a | 3.18 |
| pairIterate | 0.113 (1.00x) | 0.741 (6.56x) | n/a | n/a | 6.56 |
| hierarchyBuild | 613.621 (1.00x) | 795.143 (1.30x) | n/a | n/a | 1.30 |
| hierarchyTraverse | 68.307 (1.00x) | 83.360 (1.22x) | n/a | n/a | 1.22 |
| prefabInheritGet | 23.686 (1.00x) | 30.594 (1.29x) | n/a | n/a | 1.29 |
| observerAdd | 69.378 (1.00x) | 203.366 (2.93x) | n/a | n/a | 2.93 |
| singletonGetSet | 12.071 (1.00x) | 17.765 (1.47x) | n/a | n/a | 1.47 |
| deferAdd | 133.544 (1.00x) | 252.601 (1.89x) | n/a | n/a | 1.89 |
| multiThreadedProgress | 0.272 (1.00x) | 0.519 (1.90x) | n/a | n/a | 1.90 |
| setValue | 89.273 (1.00x) | 111.590 (1.25x) | n/a | n/a | 1.25 |
| bulkCreate | 2.212 (1.00x) | 4.674 (2.11x) | n/a | n/a | 2.11 |

