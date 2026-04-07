package uk.co.compendiumdev.performance;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.performance.helpers.CsvResultWriter;
import uk.co.compendiumdev.performance.helpers.PerformanceResult;
import uk.co.compendiumdev.performance.helpers.ProjectApiHelper;
import uk.co.compendiumdev.performance.helpers.SystemMonitor;
import uk.co.compendiumdev.sparkstart.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

public class ProjectDeletePerformanceTest {

    private static final int MAX_OBJECTS = 500;
    private static final List<String> preCreatedIds = new ArrayList<>();

    @BeforeAll
    static void ensureServiceIsRunningAndPopulate() {
        boolean serverUp = uk.co.compendiumdev.sparkstart.Port.inUse("localhost", 4567);
        if (!serverUp) {
            Environment.getBaseUri();
        }
        prePopulateProjects();
    }

    @AfterAll
    static void deleteAnyRemainingProjects() {
        for (String id : preCreatedIds) {
            ProjectApiHelper.deleteProject(id);
        }
        preCreatedIds.clear();
    }

    @Test
    void measureDeleteTimeAsObjectCountDecreases() {
        List<PerformanceResult> results = new ArrayList<>();

        int objectCount = preCreatedIds.size();

        while (!preCreatedIds.isEmpty()) {
            String idToDelete = preCreatedIds.remove(preCreatedIds.size() - 1);

            long startNs = System.nanoTime();
            Response response = ProjectApiHelper.deleteProject(idToDelete);
            long endNs = System.nanoTime();

            if (response.getStatusCode() != 200) {
                fail("Expected 200 OK on delete at objectCount=" + objectCount
                        + " but got " + response.getStatusCode());
            }

            double elapsedMs = nanosToMillis(endNs - startNs);
            long heapUsed = SystemMonitor.getHeapUsedMB();
            long heapMax = SystemMonitor.getHeapMaxMB();

            results.add(new PerformanceResult(objectCount, elapsedMs, heapUsed, heapMax));
            printProgress("PROJECT_DELETE", objectCount, elapsedMs, heapUsed);

            objectCount--;
        }

        CsvResultWriter.write("project_delete_results.csv", results);
        System.out.println("[ProjectDeleteTest] Experiment complete. " + results.size() + " data points recorded.");
    }

    private static void prePopulateProjects() {
        System.out.println("[ProjectDeleteTest] Pre-populating " + MAX_OBJECTS + " projects...");
        for (int i = 0; i < MAX_OBJECTS; i++) {
            Response response = ProjectApiHelper.createProject(
                    ProjectApiHelper.buildProject(
                            "project-" + UUID.randomUUID().toString().substring(0, 8),
                            "Project used for delete performance pre-population.",
                            false,
                            true));

            if (response.getStatusCode() != 201) {
                throw new RuntimeException("Pre-population failed at i=" + i
                        + " with status " + response.getStatusCode());
            }
            preCreatedIds.add(response.jsonPath().getString("id"));
        }
        System.out.println("[ProjectDeleteTest] Pre-population complete.");
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
