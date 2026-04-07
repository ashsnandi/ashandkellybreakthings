package uk.co.compendiumdev.performance;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.performance.helpers.CsvResultWriter;
import uk.co.compendiumdev.performance.helpers.PerformanceResult;
import uk.co.compendiumdev.performance.helpers.ProjectApiHelper;
import uk.co.compendiumdev.sparkstart.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

public class ProjectPerformanceTest {

    private static final int[] PROJECT_COUNTS = {10, 50, 100, 500, 1000};
    private static final List<String> createdProjectIds = new ArrayList<>();

    @BeforeAll
    static void ensureServiceIsRunning() {
        boolean serverUp = uk.co.compendiumdev.sparkstart.Port.inUse("localhost", 4567);
        if (!serverUp) {
            Environment.getBaseUri();
        }
    }

    @AfterAll
    static void cleanupProjects() {
        for (String projectId : createdProjectIds) {
            ProjectApiHelper.deleteProject(projectId);
        }
        createdProjectIds.clear();
    }

    @Test
    void measureProjectCreateUpdateDeletePerformance() {
        List<PerformanceResult> createResults = new ArrayList<>();
        List<PerformanceResult> updateResults = new ArrayList<>();
        List<PerformanceResult> deleteResults = new ArrayList<>();

        for (int projectCount : PROJECT_COUNTS) {
            clearProjectsForNextScenario();

            long createStartNs = System.nanoTime();
            for (int i = 0; i < projectCount; i++) {
                Response createResponse = ProjectApiHelper.createProject(
                        ProjectApiHelper.buildProject(
                                "Project " + UUID.randomUUID().toString().substring(0, 8),
                                "Generated for performance test",
                                false,
                                true));

                if (createResponse.getStatusCode() != 201) {
                    fail("Expected 201 during project create at count=" + projectCount
                            + " but got " + createResponse.getStatusCode());
                }

                createdProjectIds.add(createResponse.jsonPath().getString("id"));
            }
            long createEndNs = System.nanoTime();
            createResults.add(new PerformanceResult(
                    projectCount,
                    nanosToMillis(createEndNs - createStartNs),
                    getHeapUsedMB(),
                    getHeapMaxMB()));

            long updateStartNs = System.nanoTime();
            for (String projectId : createdProjectIds) {
                Response updateResponse = ProjectApiHelper.updateProject(
                        projectId,
                        ProjectApiHelper.buildProject(
                                "Updated " + UUID.randomUUID().toString().substring(0, 8),
                                "Updated during performance test",
                                true,
                                true));

                if (updateResponse.getStatusCode() != 200) {
                    fail("Expected 200 during project update at count=" + projectCount
                            + " but got " + updateResponse.getStatusCode());
                }
            }
            long updateEndNs = System.nanoTime();
            updateResults.add(new PerformanceResult(
                    projectCount,
                    nanosToMillis(updateEndNs - updateStartNs),
                    getHeapUsedMB(),
                    getHeapMaxMB()));

            long deleteStartNs = System.nanoTime();
            while (!createdProjectIds.isEmpty()) {
                String projectId = createdProjectIds.remove(createdProjectIds.size() - 1);
                Response deleteResponse = ProjectApiHelper.deleteProject(projectId);
                if (deleteResponse.getStatusCode() != 200) {
                    fail("Expected 200 during project delete at count=" + projectCount
                            + " but got " + deleteResponse.getStatusCode());
                }
            }
            long deleteEndNs = System.nanoTime();
            deleteResults.add(new PerformanceResult(
                    projectCount,
                    nanosToMillis(deleteEndNs - deleteStartNs),
                    getHeapUsedMB(),
                    getHeapMaxMB()));
        }

        CsvResultWriter.write("project_create_results.csv", createResults);
        CsvResultWriter.write("project_update_results.csv", updateResults);
        CsvResultWriter.write("project_delete_results.csv", deleteResults);
    }

    private static void clearProjectsForNextScenario() {
        while (!createdProjectIds.isEmpty()) {
            String projectId = createdProjectIds.remove(createdProjectIds.size() - 1);
            ProjectApiHelper.deleteProject(projectId);
        }
    }

    private static double nanosToMillis(long nanos) {
        return nanos / 1_000_000.0;
    }

    private static long getHeapUsedMB() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }

    private static long getHeapMaxMB() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.maxMemory() / (1024 * 1024);
    }
}
