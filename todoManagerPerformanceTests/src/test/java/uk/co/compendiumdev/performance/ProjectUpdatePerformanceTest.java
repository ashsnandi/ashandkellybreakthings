package uk.co.compendiumdev.performance;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.performance.helpers.CsvResultWriter;
import uk.co.compendiumdev.performance.helpers.Payloads;
import uk.co.compendiumdev.performance.helpers.PerformanceResult;
import uk.co.compendiumdev.performance.helpers.ProjectApiHelper;
import uk.co.compendiumdev.performance.helpers.SystemMonitor;
import uk.co.compendiumdev.sparkstart.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

public class ProjectUpdatePerformanceTest {

    private static final int MAX_OBJECTS = 500;
    private static final List<String> allCreatedIds = new ArrayList<>();
    private static String targetProjectId;

    @BeforeAll
    static void ensureServiceIsRunningAndCreateTarget() {
        boolean serverUp = uk.co.compendiumdev.sparkstart.Port.inUse("localhost", 4567);
        if (!serverUp) {
            Environment.getBaseUri();
        }
        createTargetProject();
    }

    @AfterAll
    static void deleteAllProjects() {
        System.out.println("[ProjectUpdateTest] Cleaning up " + allCreatedIds.size() + " projects...");
        for (String id : allCreatedIds) {
            ProjectApiHelper.deleteProject(id);
        }
        allCreatedIds.clear();
        System.out.println("[ProjectUpdateTest] Cleanup complete.");
    }

    @Test
    void measureUpdateTimeAsObjectCountGrows() {
        List<PerformanceResult> results = new ArrayList<>();

        for (int objectCount = 1; objectCount <= MAX_OBJECTS; objectCount++) {
            Payloads.ProjectPayload updatedPayload = updatedProjectPayload();

            long startNs = System.nanoTime();
            Response response = ProjectApiHelper.updateProject(targetProjectId, updatedPayload);
            long endNs = System.nanoTime();

            if (response.getStatusCode() != 200) {
                fail("Expected 200 OK on update at objectCount=" + objectCount
                        + " but got " + response.getStatusCode());
            }

            double elapsedMs = nanosToMillis(endNs - startNs);
            long heapUsed = SystemMonitor.getHeapUsedMB();
            long heapMax = SystemMonitor.getHeapMaxMB();

            results.add(new PerformanceResult(objectCount, elapsedMs, heapUsed, heapMax));
            printProgress("PROJECT_UPDATE", objectCount, elapsedMs, heapUsed);

            if (objectCount < MAX_OBJECTS) {
                addFillerProject();
            }
        }

        CsvResultWriter.write("project_update_results.csv", results);
        System.out.println("[ProjectUpdateTest] Experiment complete. " + results.size() + " data points recorded.");
    }

    private static void createTargetProject() {
        Response response = ProjectApiHelper.createProject(
                ProjectApiHelper.buildProject(
                        "target-project-" + UUID.randomUUID().toString().substring(0, 6),
                        "This project is repeatedly updated during the performance test.",
                        false,
                        true));

        if (response.getStatusCode() != 201) {
            throw new RuntimeException("Failed to create target project: " + response.getStatusCode());
        }

        targetProjectId = response.jsonPath().getString("id");
        allCreatedIds.add(targetProjectId);
    }

    private static void addFillerProject() {
        Response response = ProjectApiHelper.createProject(
                ProjectApiHelper.buildProject(
                        "filler-project-" + UUID.randomUUID().toString().substring(0, 6),
                        "Filler project used to grow total object count.",
                        false,
                        true));

        if (response.getStatusCode() != 201) {
            throw new RuntimeException("Failed to create filler project: " + response.getStatusCode());
        }

        allCreatedIds.add(response.jsonPath().getString("id"));
    }

    private static Payloads.ProjectPayload updatedProjectPayload() {
        return ProjectApiHelper.buildProject(
                "updated-project-" + UUID.randomUUID().toString().substring(0, 8),
                "Updated project payload for performance experiment.",
                true,
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
