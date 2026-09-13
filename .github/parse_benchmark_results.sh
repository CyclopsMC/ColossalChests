#!/bin/bash
# Parse benchmark results from game tests and convert to JSON format
# for use with benchmark-action/github-action-benchmark

set -e

BENCH_FILE="benchmark_results.json"
RESULTS_FILE="${PERFORMANCE_BENCHMARK_OUTPUT:-benchmark_results.txt}"

echo "Parsing benchmark results..."

if [ ! -f "$RESULTS_FILE" ]; then
    echo "Benchmark results file not found at $RESULTS_FILE"
    exit 1
fi

echo "[" > "$BENCH_FILE"

FIRST=true
while IFS= read -r line; do
    if [ -z "$line" ]; then
        continue
    fi

    # Parse the line: preset=<preset> size=<size> (avgOperationTime=<time> | bytesPerSlot=<bytes>)
    preset=$(echo "$line" | sed -n 's/.*preset=\([^ ]*\).*/\1/p')
    size=$(echo "$line" | sed -n 's/.*size=\([0-9]*\).*/\1/p')
    operationTime=$(echo "$line" | sed -n 's/.*avgOperationTime=\([0-9.]*\).*/\1/p')
    bytesPerSlot=$(echo "$line" | sed -n 's/.*bytesPerSlot=\([0-9]*\).*/\1/p')

    if [ -z "$preset" ] || [ -z "$size" ]; then
        continue
    fi

    if [ -n "$operationTime" ]; then
        if [ "$FIRST" = true ]; then
            FIRST=false
        else
            echo "," >> "$BENCH_FILE"
        fi
        cat >> "$BENCH_FILE" << EOF
  {
    "name": "CHEST LOAD: ${preset}_size_${size}",
    "unit": "operation time (ms)",
    "value": $operationTime
  }
EOF
    fi

    if [ -n "$bytesPerSlot" ]; then
        if [ "$FIRST" = true ]; then
            FIRST=false
        else
            echo "," >> "$BENCH_FILE"
        fi
        cat >> "$BENCH_FILE" << EOF
  {
    "name": "CHEST MEMORY: ${preset}_size_${size}",
    "unit": "bytes per slot",
    "value": $bytesPerSlot
  }
EOF
    fi
done < "$RESULTS_FILE"

echo "" >> "$BENCH_FILE"
echo "]" >> "$BENCH_FILE"

echo "✓ Benchmark results parsed successfully:"
echo ""
cat "$BENCH_FILE"
