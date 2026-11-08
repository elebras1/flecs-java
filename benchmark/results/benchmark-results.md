# Benchmark results

Generated: 2026-11-08 19:11:45  
Git commit: `fd556bb`  
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
| flecs C | 4.2.0 |
| flecs-java | 1.1.1 |
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
| createEmpty | 10.065 (1.00x) | 17.223 (1.71x) | 11.956 (1.19x) | 16.892 (1.68x) | 1.71 |
| create1 | 43.595 (1.00x) | 91.514 (2.10x) | 25.010 (0.57x) | 32.296 (0.74x) | 2.10 |
| create2 | 80.589 (1.00x) | 189.114 (2.35x) | 39.036 (0.48x) | 52.927 (0.66x) | 2.35 |
| createFromPrefab | 133.748 (1.00x) | 223.171 (1.67x) | 34.955 (0.26x) | 69.876 (0.52x) | 1.67 |
| destroyEmpty | 18.480 (1.00x) | 22.977 (1.24x) | 13.336 (0.72x) | 19.103 (1.03x) | 1.24 |
| destroy2 | 20.335 (1.00x) | 24.318 (1.20x) | 22.015 (1.08x) | 29.763 (1.46x) | 1.20 |
| addComponent | 33.745 (1.00x) | 59.266 (1.76x) | 17.406 (0.52x) | 53.888 (1.60x) | 1.76 |
| removeComponent | 31.784 (1.00x) | 60.783 (1.91x) | 30.819 (0.97x) | 55.967 (1.76x) | 1.91 |
| get | 3.110 (1.00x) | 18.211 (5.86x) | 1.609 (0.52x) | 3.192 (1.03x) | 5.86 |
| getSet | 4.715 (1.00x) | 18.853 (4.00x) | 3.233 (0.69x) | 3.318 (0.70x) | 4.00 |
| has | 1.550 (1.00x) | 13.307 (8.59x) | 2.480 (1.60x) | 2.815 (1.82x) | 8.59 |
| query1Read | 0.260 (1.00x) | 0.806 (3.10x) | 0.699 (2.69x) | 0.871 (3.35x) | 3.10 |
| query2ReadWrite | 0.420 (1.00x) | 2.811 (6.69x) | 1.518 (3.62x) | 1.363 (3.25x) | 6.69 |
| queryFiltered | 0.265 (1.00x) | 0.811 (3.06x) | 0.711 (2.68x) | 0.963 (3.63x) | 3.06 |
| systemRun | 0.350 (1.00x) | 2.762 (7.89x) | 1.601 (4.57x) | 17.520 (50.06x) | 7.89 |
| systemRun5 | 0.765 (1.00x) | 2.917 (3.81x) | 4.063 (5.31x) | 19.259 (25.17x) | 3.81 |
| queryCreate | 243.507 (1.00x) | 728.140 (2.99x) | n/a | n/a | 2.99 |
| lookup | 34.665 (1.00x) | 56.652 (1.63x) | n/a | n/a | 1.63 |

### N = 10,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| createEmpty | 8.997 (1.00x) | 15.995 (1.78x) | 13.646 (1.52x) | 18.424 (2.05x) | 1.78 |
| create1 | 45.846 (1.00x) | 95.236 (2.08x) | 26.941 (0.59x) | 36.770 (0.80x) | 2.08 |
| create2 | 84.403 (1.00x) | 166.453 (1.97x) | 41.061 (0.49x) | 56.019 (0.66x) | 1.97 |
| createFromPrefab | 135.768 (1.00x) | 208.295 (1.53x) | 37.258 (0.27x) | 73.189 (0.54x) | 1.53 |
| destroyEmpty | 23.706 (1.00x) | 29.184 (1.23x) | 15.650 (0.66x) | 30.107 (1.27x) | 1.23 |
| destroy2 | 28.046 (1.00x) | 31.530 (1.12x) | 23.245 (0.83x) | 50.026 (1.78x) | 1.12 |
| addComponent | 52.046 (1.00x) | 60.538 (1.16x) | 18.289 (0.35x) | 79.157 (1.52x) | 1.16 |
| removeComponent | 45.727 (1.00x) | 55.105 (1.21x) | 43.575 (0.95x) | 88.309 (1.93x) | 1.21 |
| get | 4.157 (1.00x) | 18.904 (4.55x) | 3.301 (0.79x) | 6.271 (1.51x) | 4.55 |
| getSet | 5.556 (1.00x) | 21.044 (3.79x) | 4.323 (0.78x) | 7.021 (1.26x) | 3.79 |
| has | 1.872 (1.00x) | 14.007 (7.48x) | 4.428 (2.37x) | 5.178 (2.77x) | 7.48 |
| query1Read | 0.173 (1.00x) | 0.680 (3.93x) | 1.061 (6.13x) | 2.134 (12.33x) | 3.93 |
| query2ReadWrite | 0.275 (1.00x) | 2.642 (9.61x) | 1.643 (5.98x) | 4.041 (14.69x) | 9.61 |
| queryFiltered | 0.173 (1.00x) | 0.679 (3.92x) | 1.009 (5.83x) | 2.626 (15.18x) | 3.92 |
| systemRun | 0.269 (1.00x) | 2.625 (9.78x) | 1.737 (6.47x) | 5.544 (20.65x) | 9.78 |
| systemRun5 | 0.701 (1.00x) | 2.745 (3.92x) | 4.323 (6.17x) | 9.129 (13.02x) | 3.92 |
| queryCreate | 240.847 (1.00x) | 716.259 (2.97x) | n/a | n/a | 2.97 |
| lookup | 65.281 (1.00x) | 89.832 (1.38x) | n/a | n/a | 1.38 |

### N = 100,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| createEmpty | 9.005 (1.00x) | 25.703 (2.85x) | 12.641 (1.40x) | 17.683 (1.96x) | 2.85 |
| create1 | 41.094 (1.00x) | 105.603 (2.57x) | 26.168 (0.64x) | 36.241 (0.88x) | 2.57 |
| create2 | 77.582 (1.00x) | 180.947 (2.33x) | 41.623 (0.54x) | 57.496 (0.74x) | 2.33 |
| createFromPrefab | 127.228 (1.00x) | 230.751 (1.81x) | 37.380 (0.29x) | 76.518 (0.60x) | 1.81 |
| destroyEmpty | 41.129 (1.00x) | 49.500 (1.20x) | 20.442 (0.50x) | 56.128 (1.36x) | 1.20 |
| destroy2 | 46.638 (1.00x) | 50.012 (1.07x) | 28.424 (0.61x) | 131.332 (2.82x) | 1.07 |
| addComponent | 70.627 (1.00x) | 97.316 (1.38x) | 40.781 (0.58x) | 123.064 (1.74x) | 1.38 |
| removeComponent | 66.595 (1.00x) | 78.002 (1.17x) | 102.411 (1.54x) | 140.301 (2.11x) | 1.17 |
| get | 8.844 (1.00x) | 31.966 (3.61x) | 10.394 (1.18x) | 17.353 (1.96x) | 3.61 |
| getSet | 9.586 (1.00x) | 30.721 (3.20x) | 12.619 (1.32x) | 20.523 (2.14x) | 3.20 |
| has | 3.877 (1.00x) | 18.342 (4.73x) | 11.819 (3.05x) | 10.410 (2.69x) | 4.73 |
| query1Read | 0.166 (1.00x) | 0.669 (4.04x) | 1.153 (6.96x) | 2.376 (14.35x) | 4.04 |
| query2ReadWrite | 0.269 (1.00x) | 2.621 (9.75x) | 1.699 (6.32x) | 8.057 (29.97x) | 9.75 |
| queryFiltered | 0.165 (1.00x) | 0.674 (4.07x) | 1.105 (6.68x) | 2.785 (16.84x) | 4.07 |
| systemRun | 0.259 (1.00x) | 2.619 (10.11x) | 4.735 (18.28x) | 4.033 (15.56x) | 10.11 |
| systemRun5 | 0.688 (1.00x) | 2.701 (3.93x) | 8.384 (12.19x) | 12.987 (18.88x) | 3.93 |
| queryCreate | 241.642 (1.00x) | 763.242 (3.16x) | n/a | n/a | 3.16 |
| lookup | 163.752 (1.00x) | 232.265 (1.42x) | n/a | n/a | 1.42 |

## Tier 2

### N = 1,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 26.095 (1.00x) | 48.317 (1.85x) | n/a | n/a | 1.85 |
| pairIterate | 0.220 (1.00x) | 0.922 (4.19x) | n/a | n/a | 4.19 |
| hierarchyBuild | 461.963 (1.00x) | 348.861 (0.76x) | n/a | n/a | 0.76 |
| hierarchyTraverse | 22.915 (1.00x) | 28.106 (1.23x) | n/a | n/a | 1.23 |
| prefabInheritGet | 11.710 (1.00x) | 20.372 (1.74x) | n/a | n/a | 1.74 |
| observerAdd | 68.324 (1.00x) | 396.833 (5.81x) | n/a | n/a | 5.81 |
| singletonGetSet | 7.340 (1.00x) | 14.836 (2.02x) | n/a | n/a | 2.02 |
| deferAdd | 121.248 (1.00x) | 142.092 (1.17x) | n/a | n/a | 1.17 |
| multiThreadedProgress | 12.365 (1.00x) | 10.201 (0.83x) | n/a | n/a | 0.83 |
| setValue | 35.879 (1.00x) | 89.148 (2.48x) | n/a | n/a | 2.48 |
| bulkCreate | 4.665 (1.00x) | 11.362 (2.44x) | n/a | n/a | 2.44 |

### N = 10,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 24.455 (1.00x) | 46.157 (1.89x) | n/a | n/a | 1.89 |
| pairIterate | 0.121 (1.00x) | 2.675 (22.11x) | n/a | n/a | 22.11 |
| hierarchyBuild | 307.926 (1.00x) | 345.178 (1.12x) | n/a | n/a | 1.12 |
| hierarchyTraverse | 26.984 (1.00x) | 34.607 (1.28x) | n/a | n/a | 1.28 |
| prefabInheritGet | 12.006 (1.00x) | 21.570 (1.80x) | n/a | n/a | 1.80 |
| observerAdd | 66.619 (1.00x) | 220.913 (3.32x) | n/a | n/a | 3.32 |
| singletonGetSet | 7.282 (1.00x) | 14.881 (2.04x) | n/a | n/a | 2.04 |
| deferAdd | 124.136 (1.00x) | 151.171 (1.22x) | n/a | n/a | 1.22 |
| multiThreadedProgress | 1.036 (1.00x) | 1.336 (1.29x) | n/a | n/a | 1.29 |
| setValue | 41.628 (1.00x) | 74.668 (1.79x) | n/a | n/a | 1.79 |
| bulkCreate | 2.486 (1.00x) | 4.699 (1.89x) | n/a | n/a | 1.89 |

### N = 100,000

| Benchmark | flecs C | flecs-java | artemis-odb | dominion-ecs | java/C ratio |
|---|---|---|---|---|---|
| pairAdd | 24.547 (1.00x) | 58.748 (2.39x) | n/a | n/a | 2.39 |
| pairIterate | 0.112 (1.00x) | 2.663 (23.84x) | n/a | n/a | 23.84 |
| hierarchyBuild | 420.416 (1.00x) | 454.682 (1.08x) | n/a | n/a | 1.08 |
| hierarchyTraverse | 43.460 (1.00x) | 52.186 (1.20x) | n/a | n/a | 1.20 |
| prefabInheritGet | 19.616 (1.00x) | 26.951 (1.37x) | n/a | n/a | 1.37 |
| observerAdd | 66.373 (1.00x) | 183.064 (2.76x) | n/a | n/a | 2.76 |
| singletonGetSet | 7.431 (1.00x) | 16.047 (2.16x) | n/a | n/a | 2.16 |
| deferAdd | 117.414 (1.00x) | 205.194 (1.75x) | n/a | n/a | 1.75 |
| multiThreadedProgress | 0.196 (1.00x) | 0.399 (2.04x) | n/a | n/a | 2.04 |
| setValue | 59.601 (1.00x) | 95.299 (1.60x) | n/a | n/a | 1.60 |
| bulkCreate | 2.014 (1.00x) | 3.875 (1.92x) | n/a | n/a | 1.92 |

