# Performance Benchmarking Setup

This document describes the performance benchmarking infrastructure for Colossal Chests.
Performance results are tracked in https://github.com/CyclopsMC/cyclops-performance-results

## Overview

The performance benchmarking system consists of three main components:

1. **GitHub Workflow** (`.github/workflows/performance.yml`)
   - Executes on the same triggers as CI (push and pull_request)
   - Runs the benchmark game tests on the NeoForge loader
   - Uses `benchmark-action/github-action-benchmark` to track performance evolution

2. **Game Tests** (`loader-common/src/main/java/org/cyclops/colossalchests/gametest/GameTestsPerformance.java`)
   - Benchmarks the operations that scale with chest size
   - Skipped unless `PERFORMANCE_BENCHMARK_ENABLED=true`, as they are much slower than regular game tests
   - Appends results to the file pointed at by `PERFORMANCE_BENCHMARK_OUTPUT` (default: `logs/benchmark_results.txt`, relative to the run directory)

3. **Result parsing** (`.github/parse_benchmark_results.sh`)
   - Converts the result lines into the JSON format expected by the benchmark action

## Benchmarks

All benchmarks are run on a chest of a given dimension, where `size` is either the chest dimension
(for structure benchmarks) or the resulting inventory slot count (for inventory and container benchmarks).
Every benchmark runs one warmup iteration followed by three measured iterations, and reports the mean.

| Benchmark | Description |
|-----------|-------------|
| `formation_<material>` | Building up a chest block by block, dominated by the multiblock detection that runs on every block placement |
| `formation_<material>_interfaces` | Idem, but with every wall block being an interface block |
| `revalidation_<material>` | Breaking and replacing a single wall block of a formed chest, which invalidates and revalidates the structure and its inventory |
| `inventory_construction_<material>` | Constructing the backing inventory of a chest, as happens on (in)validation and on chunk load |
| `inventory_memory_<material>` | Heap usage of that inventory, in bytes per slot |
| `container_open_<material>` | Constructing the container for a chest, as happens when a player opens it |
| `container_broadcast_<material>` | Synchronizing a single changed slot to the client, as happens on every tick in which the inventory changed |
| `container_quickmove_<material>` | Shift-clicking an item into a full chest |
| `client_inventory_fill_<material>` | Filling the client-side inventory copy, as happens when all contents are received |

## Metrics

- **Average Operation Time (ms)**: The average wall-clock time of a single operation.
- **Bytes Per Slot**: The heap usage of a chest inventory, divided by its slot count.
  This is a coarse measurement based on `Runtime`, so it is only meaningful in orders of magnitude.

## Running locally

```bash
PERFORMANCE_BENCHMARK_ENABLED=true ./gradlew :loader-neoforge:runGameTestServer
```

Results are appended to `logs/benchmark_results.txt` inside the run directory,
or to `$PERFORMANCE_BENCHMARK_OUTPUT` when that variable is set:

```bash
PERFORMANCE_BENCHMARK_ENABLED=true \
  PERFORMANCE_BENCHMARK_OUTPUT=$PWD/benchmark_results.txt \
  ./gradlew :loader-neoforge:runGameTestServer
```

The benchmarks are registered for all loaders, so they can also be run
via `:loader-fabric:runGameTestServer` or `:loader-forge:runGameTestServer`.
Note that the result file is appended to, so remove it between runs when comparing.

## Result format

```
preset=formation_wood size=9 avgOperationTime=1234.567890
preset=inventory_memory_netherite size=98415 bytesPerSlot=56
```

Which is converted into:

```json
[
  {
    "name": "CHEST LOAD: formation_wood_size_9",
    "unit": "operation time (ms)",
    "value": 1234.567890
  },
  {
    "name": "CHEST MEMORY: inventory_memory_netherite_size_98415",
    "unit": "bytes per slot",
    "value": 56
  }
]
```

## Benchmark tracking

The `benchmark-action/github-action-benchmark` GitHub action automatically:
- Stores benchmark results in the `cyclops-performance-results` repository
- Generates historical performance charts
- Alerts when performance degrades beyond the configured threshold
- Creates comments on commits

## Adding new benchmarks

1. Add a new `@GameTest` method in `GameTestsPerformance`
2. Guard it with `isEnabled()`, and report results via `report(...)`
3. The workflow will automatically execute and track the new benchmark
