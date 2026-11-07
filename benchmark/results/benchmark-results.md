# Benchmark results

Generated: 2026-11-01 10:11:05  
Git commit: `608995a`  
Mode: full

## Machine

| Key | Value |
|---|---|
| os | Linux 7.0.0-28-generic |
| kernel | 7.0.0-28-generic |
| cpu | AMD Ryzen 5 5600GT with Radeon Graphics |
| cores | 12 |
| ram_gb | 15 |
| jvm | GraalVM Community 25.0.2 |
| gcc | gcc (Ubuntu 13.3.0-6ubuntu2~24.04.1) 13.3.0 |

## Library versions

| Library | Version |
|---|---|
| flecs C | 4.1.6 |
| flecs-java | 1.0.1 |
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
| createEmpty | 8.275 (1.00x) | 16.561 (2.00x) | 11.247 (1.36x) | 17.627 (2.13x) | 2.00 |
| create1 | 52.000 (1.00x) | 101.229 (1.95x) | 25.069 (0.48x) | 37.214 (0.72x) | 1.95 |
| create2 | 94.415 (1.00x) | 182.845 (1.94x) | 39.458 (0.42x) | 55.138 (0.58x) | 1.94 |
| createFromPrefab | 124.285 (1.00x) | 192.279 (1.55x) | 35.395 (0.28x) | 82.911 (0.67x) | 1.55 |
| destroyEmpty | 19.930 (1.00x) | 26.667 (1.34x) | 13.477 (0.68x) | 18.930 (0.95x) | 1.34 |
| destroy2 | 23.800 (1.00x) | 28.896 (1.21x) | 21.502 (0.90x) | 29.460 (1.24x) | 1.21 |
| addComponent | 37.180 (1.00x) | 58.454 (1.57x) | 17.635 (0.47x) | 53.561 (1.44x) | 1.57 |
| removeComponent | 33.280 (1.00x) | 54.989 (1.65x) | 32.748 (0.98x) | 58.465 (1.76x) | 1.65 |
| get | 3.440 (1.00x) | 17.892 (5.20x) | 1.720 (0.50x) | 3.273 (0.95x) | 5.20 |
| getSet | 4.955 (1.00x) | 22.498 (4.54x) | 3.449 (0.70x) | 3.433 (0.69x) | 4.54 |
| has | 1.490 (1.00x) | 13.775 (9.25x) | 3.764 (2.53x) | 2.702 (1.81x) | 9.25 |
| query1Read | 0.260 (1.00x) | 0.812 (3.12x) | 0.689 (2.65x) | 0.864 (3.32x) | 3.12 |
| query2ReadWrite | 0.350 (1.00x) | 2.791 (7.97x) | 1.513 (4.32x) | 1.369 (3.91x) | 7.97 |
| queryFiltered | 0.260 (1.00x) | 0.825 (3.17x) | 0.699 (2.69x) | 0.948 (3.65x) | 3.17 |
| systemRun | 0.350 (1.00x) | 2.760 (7.89x) | 1.622 (4.63x) | 17.550 (50.14x) | 7.89 |
| systemRun5 | 0.765 (1.00x) | 2.878 (3.76x) | 4.216 (5.51x) | 19.421 (25.39x) | 3.76 |
| queryCreate | 221.525 (1.00x) | 701.048 (3.16x) | n/a | n/a | 3.16 |
| lookup | 61.015 (1.00x) | 85.750 (1.41x) | n/a | n/a | 1.41 |

### N = 10,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| createEmpty | 11.343 (1.00x) | 15.561 (1.37x) | 13.035 (1.15x) | 18.962 (1.67x) | 1.37 |
| create1 | 55.928 (1.00x) | 97.035 (1.73x) | 26.949 (0.48x) | 38.014 (0.68x) | 1.73 |
| create2 | 100.470 (1.00x) | 181.588 (1.81x) | 41.599 (0.41x) | 53.205 (0.53x) | 1.81 |
| createFromPrefab | 129.629 (1.00x) | 179.666 (1.39x) | 37.662 (0.29x) | 76.529 (0.59x) | 1.39 |
| destroyEmpty | 26.250 (1.00x) | 33.108 (1.26x) | 16.042 (0.61x) | 26.031 (0.99x) | 1.26 |
| destroy2 | 34.319 (1.00x) | 35.937 (1.05x) | 22.903 (0.67x) | 50.055 (1.46x) | 1.05 |
| addComponent | 61.514 (1.00x) | 65.480 (1.06x) | 19.559 (0.32x) | 72.570 (1.18x) | 1.06 |
| removeComponent | 53.959 (1.00x) | 61.309 (1.14x) | 46.225 (0.86x) | 97.850 (1.81x) | 1.14 |
| get | 4.832 (1.00x) | 21.206 (4.39x) | 4.398 (0.91x) | 6.454 (1.34x) | 4.39 |
| getSet | 5.597 (1.00x) | 24.766 (4.42x) | 4.167 (0.74x) | 6.655 (1.19x) | 4.42 |
| has | 1.920 (1.00x) | 14.319 (7.46x) | 5.262 (2.74x) | 5.154 (2.68x) | 7.46 |
| query1Read | 0.174 (1.00x) | 0.680 (3.91x) | 1.060 (6.09x) | 2.140 (12.30x) | 3.91 |
| query2ReadWrite | 0.267 (1.00x) | 2.633 (9.86x) | 1.631 (6.11x) | 4.114 (15.41x) | 9.86 |
| queryFiltered | 0.174 (1.00x) | 0.681 (3.92x) | 0.992 (5.70x) | 2.679 (15.40x) | 3.92 |
| systemRun | 0.270 (1.00x) | 2.639 (9.77x) | 1.723 (6.38x) | 5.442 (20.16x) | 9.77 |
| systemRun5 | 0.704 (1.00x) | 2.689 (3.82x) | 4.395 (6.24x) | 9.147 (12.99x) | 3.82 |
| queryCreate | 224.585 (1.00x) | 761.429 (3.39x) | n/a | n/a | 3.39 |
| lookup | 91.727 (1.00x) | 121.567 (1.33x) | n/a | n/a | 1.33 |

### N = 100,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| createEmpty | 12.836 (1.00x) | 15.498 (1.21x) | 12.172 (0.95x) | 17.849 (1.39x) | 1.21 |
| create1 | 54.346 (1.00x) | 107.819 (1.98x) | 26.355 (0.48x) | 35.854 (0.66x) | 1.98 |
| create2 | 101.200 (1.00x) | 194.587 (1.92x) | 41.429 (0.41x) | 58.938 (0.58x) | 1.92 |
| createFromPrefab | 129.458 (1.00x) | 196.500 (1.52x) | 37.181 (0.29x) | 87.611 (0.68x) | 1.52 |
| destroyEmpty | 44.584 (1.00x) | 53.280 (1.20x) | 19.414 (0.44x) | 52.110 (1.17x) | 1.20 |
| destroy2 | 56.484 (1.00x) | 53.972 (0.96x) | 29.584 (0.52x) | 133.856 (2.37x) | 0.96 |
| addComponent | 80.102 (1.00x) | 109.060 (1.36x) | 38.068 (0.48x) | 119.694 (1.49x) | 1.36 |
| removeComponent | 80.753 (1.00x) | 94.993 (1.18x) | 112.037 (1.39x) | 151.023 (1.87x) | 1.18 |
| get | 9.548 (1.00x) | 37.139 (3.89x) | 10.588 (1.11x) | 17.684 (1.85x) | 3.89 |
| getSet | 9.120 (1.00x) | 36.535 (4.01x) | 11.909 (1.31x) | 21.181 (2.32x) | 4.01 |
| has | 4.625 (1.00x) | 22.629 (4.89x) | 11.501 (2.49x) | 16.985 (3.67x) | 4.89 |
| query1Read | 0.166 (1.00x) | 0.669 (4.03x) | 1.176 (7.08x) | 2.235 (13.47x) | 4.03 |
| query2ReadWrite | 0.260 (1.00x) | 2.621 (10.07x) | 1.736 (6.67x) | 8.416 (32.34x) | 10.07 |
| queryFiltered | 0.166 (1.00x) | 0.670 (4.04x) | 1.160 (7.00x) | 2.757 (16.62x) | 4.04 |
| systemRun | 0.263 (1.00x) | 2.618 (9.94x) | 4.614 (17.52x) | 3.993 (15.16x) | 9.94 |
| systemRun5 | 0.696 (1.00x) | 2.753 (3.96x) | 8.447 (12.14x) | 12.954 (18.62x) | 3.96 |
| queryCreate | 222.639 (1.00x) | 771.688 (3.47x) | n/a | n/a | 3.47 |
| lookup | 244.269 (1.00x) | 305.722 (1.25x) | n/a | n/a | 1.25 |

## Tier 2

### N = 1,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 31.620 (1.00x) | 49.688 (1.57x) | n/a | n/a | 1.57 |
| pairIterate | 0.210 (1.00x) | 0.911 (4.34x) | n/a | n/a | 4.34 |
| hierarchyBuild | 364.459 (1.00x) | 397.892 (1.09x) | n/a | n/a | 1.09 |
| hierarchyTraverse | 21.625 (1.00x) | 28.149 (1.30x) | n/a | n/a | 1.30 |
| prefabInheritGet | 14.740 (1.00x) | 24.916 (1.69x) | n/a | n/a | 1.69 |
| observerAdd | 66.755 (1.00x) | 402.792 (6.03x) | n/a | n/a | 6.03 |
| singletonGetSet | 12.320 (1.00x) | 18.236 (1.48x) | n/a | n/a | 1.48 |
| deferAdd | 107.115 (1.00x) | 136.102 (1.27x) | n/a | n/a | 1.27 |
| multiThreadedProgress | 9.625 (1.00x) | 10.275 (1.07x) | n/a | n/a | 1.07 |
| setValue | 45.430 (1.00x) | 83.291 (1.83x) | n/a | n/a | 1.83 |
| bulkCreate | 5.325 (1.00x) | 11.473 (2.15x) | n/a | n/a | 2.15 |

### N = 10,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 30.679 (1.00x) | 49.834 (1.62x) | n/a | n/a | 1.62 |
| pairIterate | 0.121 (1.00x) | 0.813 (6.72x) | n/a | n/a | 6.72 |
| hierarchyBuild | 398.598 (1.00x) | 421.078 (1.06x) | n/a | n/a | 1.06 |
| hierarchyTraverse | 27.423 (1.00x) | 37.548 (1.37x) | n/a | n/a | 1.37 |
| prefabInheritGet | 15.196 (1.00x) | 25.520 (1.68x) | n/a | n/a | 1.68 |
| observerAdd | 66.150 (1.00x) | 232.396 (3.51x) | n/a | n/a | 3.51 |
| singletonGetSet | 11.849 (1.00x) | 18.129 (1.53x) | n/a | n/a | 1.53 |
| deferAdd | 119.916 (1.00x) | 150.226 (1.25x) | n/a | n/a | 1.25 |
| multiThreadedProgress | 1.038 (1.00x) | 1.298 (1.25x) | n/a | n/a | 1.25 |
| setValue | 50.420 (1.00x) | 84.478 (1.68x) | n/a | n/a | 1.68 |
| bulkCreate | 2.734 (1.00x) | 4.359 (1.59x) | n/a | n/a | 1.59 |

### N = 100,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 31.056 (1.00x) | 63.298 (2.04x) | n/a | n/a | 2.04 |
| pairIterate | 0.114 (1.00x) | 0.798 (7.01x) | n/a | n/a | 7.01 |
| hierarchyBuild | 513.735 (1.00x) | 531.707 (1.03x) | n/a | n/a | 1.03 |
| hierarchyTraverse | 57.432 (1.00x) | 57.408 (1.00x) | n/a | n/a | 1.00 |
| prefabInheritGet | 23.574 (1.00x) | 34.313 (1.46x) | n/a | n/a | 1.46 |
| observerAdd | 66.338 (1.00x) | 183.071 (2.76x) | n/a | n/a | 2.76 |
| singletonGetSet | 11.862 (1.00x) | 18.297 (1.54x) | n/a | n/a | 1.54 |
| deferAdd | 118.357 (1.00x) | 194.896 (1.65x) | n/a | n/a | 1.65 |
| multiThreadedProgress | 0.197 (1.00x) | 0.400 (2.03x) | n/a | n/a | 2.03 |
| setValue | 71.486 (1.00x) | 98.398 (1.38x) | n/a | n/a | 1.38 |
| bulkCreate | 2.218 (1.00x) | 3.858 (1.74x) | n/a | n/a | 1.74 |

