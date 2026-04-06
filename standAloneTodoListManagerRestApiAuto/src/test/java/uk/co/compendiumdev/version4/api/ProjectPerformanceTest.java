package uk.co.compendiumdev.version4.api;

import io.restassured.response.Response;
import java.util.*;

/**
 * Performance Testing Suite for Projects
 * Measures create, update, and delete operations on projects
 * Tracks timing and system resources as project count increases
 */
public class ProjectPerformanceTest {

    private static final Random random = new Random();
    private static final List<Integer> projectIds = new ArrayList<>();
    private static final Map<String, PerformanceMetrics> results = new HashMap<>();

    static class PerformanceMetrics {
        String operation;
        int projectCount;
        long startTime;
        long endTime;
        long durationMs;
        long memoryBefore;
        long memoryAfter;
        double memoryUsedMB;

        public PerformanceMetrics(String operation, int projectCount) {
            this.operation = operation;
            this.projectCount = projectCount;
            this.memoryBefore = getMemoryUsageMB();
            this.startTime = System.currentTimeMillis();
        }

        public void stop() {
            this.endTime = System.currentTimeMillis();
            this.durationMs = endTime - startTime;
            this.memoryAfter = getMemoryUsageMB();
            this.memoryUsedMB = Math.max(0, memoryAfter - memoryBefore);
        }

        @Override
        public String toString() {
            return String.format("[%s] Projects: %d | Time: %d ms | Memory: %.2f MB",
                    operation, projectCount, durationMs, memoryUsedMB);
        }
    }

    /**
     * Test 1: Create X projects and measure time
     */
    public static void testCreateProjects(int numberOfProjects) {
        System.out.println("\n========== TEST: CREATE " + numberOfProjects + " PROJECTS ==========");
        PerformanceMetrics metrics = new PerformanceMetrics("CREATE", numberOfProjects);

        for (int i = 0; i < numberOfProjects; i++) {
            Payloads.ProjectPayload project = generateRandomProject();
            Response response = Api.createProject(project);

            if (response.getStatusCode() == 201) {
                int projectId = response.jsonPath().getInt("id");
                projectIds.add(projectId);
                
                if ((i + 1) % Math.max(1, numberOfProjects / 10) == 0) {
                    System.out.println("  Created " + (i + 1) + " projects...");
                }
            } else {
                System.err.println("  Failed to create project #" + (i + 1) + 
                                 ": Status " + response.getStatusCode());
            }
        }

        metrics.stop();
        logMetrics(metrics);
        results.put("CREATE_" + numberOfProjects, metrics);
    }

    /**
     * Test 2: Update existing projects and measure time
     */
    public static void testUpdateProjects() {
        if (projectIds.isEmpty()) {
            System.out.println("\n[WARNING] No projects to update. Run testCreateProjects first.");
            return;
        }

        System.out.println("\n========== TEST: UPDATE " + projectIds.size() + " PROJECTS ==========");
        PerformanceMetrics metrics = new PerformanceMetrics("UPDATE", projectIds.size());

        for (int projectId : projectIds) {
            Payloads.ProjectPayload updatedProject = generateRandomProject();
            Response response = Api.updateProject(projectId, updatedProject);

            if (response.getStatusCode() != 200) {
                System.err.println("  Failed to update project #" + projectId + 
                                 ": Status " + response.getStatusCode());
            }
        }

        metrics.stop();
        logMetrics(metrics);
        results.put("UPDATE_" + projectIds.size(), metrics);
    }

    /**
     * Test 3: Delete projects and measure time
     */
    public static void testDeleteProjects() {
        if (projectIds.isEmpty()) {
            System.out.println("\n[WARNING] No projects to delete.");
            return;
        }

        System.out.println("\n========== TEST: DELETE " + projectIds.size() + " PROJECTS ==========");
        PerformanceMetrics metrics = new PerformanceMetrics("DELETE", projectIds.size());
        
        List<Integer> idsToDelete = new ArrayList<>(projectIds);

        for (int projectId : idsToDelete) {
            Response response = Api.deleteProject(projectId);

            if (response.getStatusCode() != 200 && response.getStatusCode() != 204) {
                System.err.println("  Failed to delete project #" + projectId + 
                                 ": Status " + response.getStatusCode());
            }
        }

        metrics.stop();
        projectIds.clear();
        logMetrics(metrics);
        results.put("DELETE_" + idsToDelete.size(), metrics);
    }

    /**
     * Run performance experiments with increasing project counts
     * This is the main test harness for Part C
     */
    public static void runPerformanceExperiments(int[] projectCounts) {
        System.out.println("\n\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║        PROJECT PERFORMANCE TEST SUITE - PART C             ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        printSystemInfo();

        for (int count : projectCounts) {
            System.gc(); // Suggest garbage collection between tests
            
            // Create test
            testCreateProjects(count);
            
            // Update test
            testUpdateProjects();
            
            // Delete test
            testDeleteProjects();

            System.out.println("\n" + "-".repeat(60));
        }

        printSummaryReport();
    }

    /**
     * Generate a random project payload for testing
     */
    private static Payloads.ProjectPayload generateRandomProject() {
        Payloads.ProjectPayload project = new Payloads.ProjectPayload();
        project.title = "Test Project " + UUID.randomUUID().toString().substring(0, 8);
        project.description = "Performance test project created at " + System.currentTimeMillis();
        project.completed = random.nextBoolean();
        project.active = random.nextBoolean();
        return project;
    }

    /**
     * Get current memory usage in MB
     */
    private static long getMemoryUsageMB() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }

    /**
     * Log performance metrics
     */
    private static void logMetrics(PerformanceMetrics metrics) {
        System.out.println("\n✓ " + metrics);
    }

    /**
     * Print system information
     */
    private static void printSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        System.out.println("\nSystem Information:");
        System.out.println("  OS: " + System.getProperty("os.name") + " " + 
                          System.getProperty("os.version"));
        System.out.println("  Java Version: " + System.getProperty("java.version"));
        System.out.println("  Available Processors: " + runtime.availableProcessors());
        System.out.println("  Max Memory: " + (runtime.maxMemory() / (1024 * 1024)) + " MB");
        System.out.println("  Initial Memory: " + (getMemoryUsageMB()) + " MB");
    }

    /**
     * Print summary report of all performance tests
     */
    private static void printSummaryReport() {
        System.out.println("\n\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║              PERFORMANCE TEST SUMMARY REPORT               ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        List<PerformanceMetrics> sortedResults = new ArrayList<>(results.values());
        sortedResults.sort((a, b) -> {
            if (!a.operation.equals(b.operation)) {
                return a.operation.compareTo(b.operation);
            }
            return Integer.compare(a.projectCount, b.projectCount);
        });

        String currentOp = "";
        for (PerformanceMetrics metric : sortedResults) {
            if (!currentOp.equals(metric.operation)) {
                currentOp = metric.operation;
                System.out.println("\n" + currentOp + " OPERATIONS:");
            }
            System.out.println("  " + metric);
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("Export these results for charting (time vs project count)");
        System.out.println("Recommend using system monitoring (vmstat, top) during tests");
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * Main method to run performance tests
     * Usage: java -cp ... ProjectPerformanceTest
     */
    public static void main(String[] args) {
        // Define test scenarios: number of projects to create
        int[] projectCounts = {10, 50, 100, 500, 1000};
        
        runPerformanceExperiments(projectCounts);
    }
}
