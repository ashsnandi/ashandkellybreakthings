package uk.co.compendiumdev.performance;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.performance.helpers.CsvResultWriter;
import uk.co.compendiumdev.performance.helpers.Payloads;
import uk.co.compendiumdev.performance.helpers.PerformanceResult;
import uk.co.compendiumdev.performance.helpers.SystemMonitor;
import uk.co.compendiumdev.performance.helpers.ProjectApiHelper;
import uk.co.compendiumdev.sparkstart.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

public class ProjectCreatePerformanceTest {

    private static final int MAX_OBJECTS = 500;
    private static final List<String> createdProjectIds = new ArrayList<>();

    @BeforeAll
    static void ensureServiceIsRunning() {
        boolean serverUp = uk.co.compendiumdev.sparkstart.Port.inUse("localhost", 4567);
        if (!serverUp) {
            Environment.getBaseUri();
        }
    }

    @AfterAll
    static void deleteAllCreatedProjects() {
        System.out.println("[ProjectCreateTest] Cleaning up " + createdProjectIds.size() + " projects...");
        for (String id : createdProjectIds) {
            ProjectApiHelper.deleteProject(id);
        }
        createdProjectIds.clear();
        System.out.println("[ProjectCreateTest] Cleanup complete.");
    }

    @Test
    void measureCreateTimeAsObjectCountGrows() {
        List<PerformanceResult> results = new ArrayList<>();

        for (int objectCount = 0; objectCount < MAX_OBJECTS; objectCount++) {
            Payloads.ProjectPayload project = randomProject(false);

            long startNs = System.nanoTime();
            Response response = ProjectApiHelper.createProject(project);
            long endNs = System.nanoTime();

            if (response.getStatusCode() != 201) {
                fail("Expected 201 Created at objectCount=" + objectCount
                        + " but got " + response.getStatusCode());
            }

            createdProjectIds.add(response.jsonPath().getString("id"));

            double elapsedMs = nanosToMillis(endNs - startNs);
            long heapUsed = SystemMonitor.getHeapUsedMB();
            long heapMax = SystemMonitor.getHeapMaxMB();

            results.add(new PerformanceResult(objectCount, elapsedMs, heapUsed, heapMax));
            printProgress("PROJECT_CREATE", objectCount, elapsedMs, heapUsed);
        }

        CsvResultWriter.write("project_create_results.csv", results);
        System.out.println("[ProjectCreateTest] Experiment complete. " + results.size() + " data points recorded.");
    }

    private static Payloads.ProjectPayload randomProject(boolean completed) {
        return ProjectApiHelper.buildProject(
                "project " + UUID.randomUUID().toString().substring(0, 8),
                "Generated project for performance testing",
                completed,
                true
        );
    }

    private static double nanosToMillis(long nanos) {
        return nanos / 1_000_000.0;
    }

    private static void printProgress(String operation, int objectCount, double ms, long heapMB) {
        if (objectCount % 50 == 0) {
            System.out.printf("[%s] objectCount=%d | time=%.3f ms | heap=%d MB%n",
                    operation, objectCount, ms, heapMB);
        }
    }
}
