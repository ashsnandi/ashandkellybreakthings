#!/bin/bash

# Project Performance Testing Script for Part C
# Monitors CPU and memory while running performance tests

echo "=========================================="
echo "Project Performance Testing - Part C"
echo "=========================================="

# Check if REST API is running
if ! curl -s http://localhost:8080/projects > /dev/null 2>&1; then
    echo "ERROR: REST API is not running on http://localhost:8080"
    echo "Please start the Todo Manager REST API first"
    echo ""
    echo "You can start it with:"
    echo "  cd standAloneTodoListManagerRestApi"
    echo "  mvn spring-boot:run"
    exit 1
fi

# Create output directory for metrics
METRICS_DIR="performance_metrics"
TEST_TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
TEST_DIR="$METRICS_DIR/$TEST_TIMESTAMP"

mkdir -p "$TEST_DIR"

echo "Test Directory: $TEST_DIR"
echo "Starting system monitoring..."
echo ""

# Start vmstat monitoring in background (1 second interval)
VMSTAT_LOG="$TEST_DIR/vmstat.log"
vmstat 1 > "$VMSTAT_LOG" 2>&1 &
VMSTAT_PID=$!

echo "vmstat monitoring started (PID: $VMSTAT_PID)"
echo "Logging to: $VMSTAT_LOG"
echo ""

# Wait a moment for vmstat to initialize
sleep 2

# Run the performance tests
echo "Running ProjectPerformanceTest..."
java -cp "target/classes:target/dependency/*" \
     uk.co.compendiumdev.version4.api.ProjectPerformanceTest \
     | tee "$TEST_DIR/performance_test.log"

TEST_STATUS=$?

echo ""
echo "Test completed with status: $TEST_STATUS"
echo ""

# Stop vmstat monitoring
echo "Stopping system monitoring..."
kill $VMSTAT_PID 2>/dev/null
wait $VMSTAT_PID 2>/dev/null

echo "System monitoring saved to: $VMSTAT_LOG"
echo ""

# Summary
echo "=========================================="
echo "Performance Test Results Summary"
echo "=========================================="
echo "Test Directory: $TEST_DIR"
echo "Files generated:"
echo "  - performance_test.log: Test results and timings"
echo "  - vmstat.log: CPU and memory statistics"
echo ""
echo "Next steps:"
echo "1. Review performance_test.log for timing results"
echo "2. Use vmstat.log to create memory and CPU charts"
echo "3. Extract data for Part C report (5-10 pages)"
echo ""
echo "Manual monitoring alternative:"
echo "  In another terminal, run: vmstat 1"
echo "  This shows: procs, memory, swap, disk I/O, system, CPU"
echo ""
