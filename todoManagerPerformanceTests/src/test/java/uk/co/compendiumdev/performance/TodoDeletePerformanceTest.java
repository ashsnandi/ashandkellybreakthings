package uk.co.compendiumdev.performance;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.performance.helpers.CsvResultWriter;
import uk.co.compendiumdev.performance.helpers.PerformanceResult;
import uk.co.compendiumdev.performance.helpers.Payloads;
import uk.co.compendiumdev.performance.helpers.RandomDataGenerator;
import uk.co.compendiumdev.performance.helpers.SystemMonitor;
import uk.co.compendiumdev.performance.helpers.TodoApiHelper;
import uk.co.compendiumdev.sparkstart.Environment;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Performance experiment: measures how long it takes to DELETE a todo
 * as the total number of objects in the system decreases from MAX_OBJECTS to 1.
 *
 * Experiment:
 *   - Pre-populate the system with MAX_OBJECTS todos.
 *   - Delete them one by one, recording the time for each delete.
 *   - objectCount reported is the count present BEFORE the delete.
 *
 * Output: performance_results/delete_results.csv
 *   Columns: objectCount, operationTimeMs, heapUsedMB, heapMaxMB
 */
public class TodoDeletePerformanceTest {

    private static final int MAX_OBJECTS = 500;
    private static final List<String> preCreatedIds = new ArrayList<>();

    @BeforeAll
    static void ensureServiceIsRunningAndPopulate() {
        boolean serverUp = uk.co.compendiumdev.sparkstart.Port.inUse("localhost", 4567);
        if (!serverUp) {
            Environment.getBaseUri();
        }
        prePopulateTodos();
    }

    @AfterAll
    static void deleteAnyRemainingTodos() {
        // Safety net: delete anything not consumed by the test
        for (String id : preCreatedIds) {
            TodoApiHelper.deleteTodo(id);
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
            Response response = TodoApiHelper.deleteTodo(idToDelete);
            long endNs = System.nanoTime();

            if (response.getStatusCode() != 200) {
                fail("Expected 200 OK on delete at objectCount=" + objectCount
                        + " but got " + response.getStatusCode());
            }

            double elapsedMs = nanosToMillis(endNs - startNs);
            long heapUsed = SystemMonitor.getHeapUsedMB();
            long heapMax = SystemMonitor.getHeapMaxMB();

            results.add(new PerformanceResult(objectCount, elapsedMs, heapUsed, heapMax));
            printProgress("DELETE", objectCount, elapsedMs, heapUsed);

            objectCount--;
        }

        CsvResultWriter.write("delete_results.csv", results);
        System.out.println("[DeleteTest] Experiment complete. " + results.size() + " data points recorded.");
    }

    private static void prePopulateTodos() {
        System.out.println("[DeleteTest] Pre-populating " + MAX_OBJECTS + " todos...");
        for (int i = 0; i < MAX_OBJECTS; i++) {
            Payloads.TodoPayload todo = RandomDataGenerator.randomTodo();
            Response response = TodoApiHelper.createTodo(todo);
            if (response.getStatusCode() != 201) {
                throw new RuntimeException("Pre-population failed at i=" + i
                        + " with status " + response.getStatusCode());
            }
            preCreatedIds.add(response.jsonPath().getString("id"));
        }
        System.out.println("[DeleteTest] Pre-population complete.");
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
