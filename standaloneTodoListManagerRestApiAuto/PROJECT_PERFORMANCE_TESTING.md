# Project Performance Testing - Part C

## Your Assignment
You are responsible for **Performance Testing of Projects** in Part C. This involves measuring how the REST API handles creating, updating, and deleting projects as the project count increases.

## What You Need to Do

### 1. **Run Performance Tests**
The performance test suite measures:
- **CREATE**: Time to create X projects
- **UPDATE**: Time to update existing projects  
- **DELETE**: Time to delete all projects
- **Memory usage** for each operation
- **System metrics** (CPU, memory) during execution

### 2. **Conduct Experiments**
Run performance tests with increasing project counts:
- 10 projects (baseline)
- 50 projects
- 100 projects
- 500 projects
- 1000 projects (or as high as your API can handle)

### 3. **Collect System Metrics**
Monitor CPU and memory during tests using Linux tools:
- **vmstat** (recommended - automatic in script)
- OR **top** (manual in separate terminal)
- OR **Activity Monitor** (Mac) or **perfmon** (Windows)

### 4. **Generate Data for Charts**
Document:
- Transaction time (ms) vs number of projects
- Memory usage (MB) vs number of projects
- CPU utilization (%) during tests

## How to Run

### Prerequisites
1. Start the REST API:
```bash
cd standAloneTodoListManagerRestApi
mvn spring-boot:run
# API should be running on http://localhost:8080
```

2. Build the test project:
```bash
cd standaloneTodoListManagerRestApiAuto
mvn clean compile
```

### Option A: Automated Testing (Recommended)
```bash
cd standaloneTodoListManagerRestApiAuto
chmod +x run_project_performance_tests.sh
./run_project_performance_tests.sh
```

This script:
- Verifies the API is running
- Starts vmstat monitoring automatically
- Runs all performance tests
- Saves results to `performance_metrics/<timestamp>/`

### Option B: Manual Testing
```bash
cd standaloneTodoListManagerRestApiAuto

# Terminal 1: Monitor system metrics
vmstat 1

# Terminal 2: Run tests
mvn test -Dtest=ProjectPerformanceTest -DargLine="-Xmx2g"
```

Or run directly:
```bash
java -cp "target/classes:target/dependency/*" \
     uk.co.compendiumdev.version4.api.ProjectPerformanceTest
```

## Understanding the Results

### Performance Test Output
```
[CREATE] Projects: 100 | Time: 2345 ms | Memory: 15.32 MB
[UPDATE] Projects: 100 | Time: 1892 ms | Memory: 8.15 MB
[DELETE] Projects: 100 | Time: 1204 ms | Memory: 2.11 MB
```

This tells you:
- How long each operation takes
- Memory consumed per operation
- Performance trends as project count increases

### vmstat Output
```
procs -----------memory---------- ---swap-- -----io---- -system-- ------cpu-----
 r  b   swpd   free   buff  cache   si   so    bi    bo   in   cs us sy id wa st
 1  0 145556 233248  10240 489652    0    0    10    20   45  131  7  3 87  3  0
```

Key columns:
- **free**: Free memory (MB)
- **us**: User CPU time (%)
- **sy**: System CPU time (%)
- **id**: Idle time (%)
- **wa**: Wait time for I/O (%)

## Performance Testing Best Practices

1. **Warm up**: Run a small test first to warm up JVM
2. **Repeat**: Run each test at least 3 times and average results
3. **System state**: Close other programs to reduce interference
4. **Record conditions**: Note system load, other processes, network latency
5. **Vary load**: Test with different numbers of projects to find breaking points

## Data Collection for Report

Collect data for each experiment:
- Number of projects created
- Time to create (ms)
- Time to update (ms)  
- Time to delete (ms)
- Memory before operation (MB)
- Memory after operation (MB)
- CPU utilization during operation (%)

Export to spreadsheet and create charts showing:
1. Operation Time vs Project Count
2. Memory Usage vs Project Count
3. CPU Utilization vs Project Count

## Expected Deliverables

For your part of Part C report, include:
1. **Performance Test Suite** - describe the test design
2. **Execution Results** - charts showing time/memory/CPU trends
3. **Performance Analysis** - interpretation of results
4. **Bottleneck Identification** - where does performance degrade?
5. **Recommendations** - what code changes could improve performance?

## Troubleshooting

### API connection refused
- Check if REST API is running: `curl http://localhost:8080/projects`
- Verify port 8080 is correct
- Check firewall settings

### Out of Memory errors
- Reduce number of projects per test
- Run with larger heap: `java -Xmx4g -Xms2g`
- Clear projects between tests

### Tests running very slowly
- API may be overloaded - close other applications
- Check if another test is running
- Database operations may be slow - document this

## Files Created

- `ProjectPerformanceTest.java` - Main performance test suite
- `Api.java` (updated) - Added updateProject(), deleteProject() methods
- `run_project_performance_tests.sh` - Automated test script with monitoring
- `performance_metrics/<timestamp>/` - Test results directory

## Next Steps

1. Run initial test with 10-100 projects
2. Monitor the results and adjust test parameters
3. Run full experiment suite (10, 50, 100, 500, 1000 projects)
4. Collect vmstat data
5. Create charts and analyze trends
6. Document recommendations in Part C report
