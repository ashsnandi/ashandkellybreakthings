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

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Performance experiment: measures how long it takes to UPDATE (PUT) a todo
 * as the total number of objects in the system grows from 1 to MAX_OBJECTS.
 *
 * Experiment:
 *   - Create a single "target" todo at the start.
 *   - Each iteration: measure time to PUT-update the target, then add one more filler todo.
 *   - objectCount reported is the total count in the system when the update is measured.
 *
 * Output: performance_results/update_results.csv
 *   Columns: objectCount, operationTimeMs, heapUsedMB, heapMaxMB
 */
public class TodoUpdatePerformanceTest {

    private static final int MAX_OBJECTS = 500;
    private static final List<String> allCreatedIds = new ArrayList<>();
    private static String targetTodoId;

    @BeforeAll
    static void ensureServiceIsRunningAndCreateTarget() {
        boolean serverUp = uk.co.compendiumdev.sparkstart.Port.inUse("localhost", 4567);
        if (!serverUp) {
            Environment.getBaseUri();
        }
        createTargetTodo();
    }

    @AfterAll
    static void deleteAllTodos() {
        System.out.println("[UpdateTest] Cleaning up " + allCreatedIds.size() + " todos...");
        for (String id : allCreatedIds) {
            TodoApiHelper.deleteTodo(id);
        }
        allCreatedIds.clear();
        System.out.println("[UpdateTest] Cleanup complete.");
    }

    @Test
    void measureUpdateTimeAsObjectCountGrows() {
        List<PerformanceResult> results = new ArrayList<>();

        // objectCount starts at 1 (the target todo already exists)
        for (int objectCount = 1; objectCount <= MAX_OBJECTS; objectCount++) {
            Payloads.TodoPayload updatedPayload = RandomDataGenerator.randomUpdatedTodo("target");

            long startNs = System.nanoTime();
            Response response = TodoApiHelper.updateTodo(targetTodoId, updatedPayload);
            long endNs = System.nanoTime();

            if (response.getStatusCode() != 200) {
                fail("Expected 200 OK on update at objectCount=" + objectCount
                        + " but got " + response.getStatusCode());
            }

            double elapsedMs = nanosToMillis(endNs - startNs);
            long heapUsed = SystemMonitor.getHeapUsedMB();
            long heapMax = SystemMonitor.getHeapMaxMB();

            results.add(new PerformanceResult(objectCount, elapsedMs, heapUsed, heapMax));
            printProgress("UPDATE", objectCount, elapsedMs, heapUsed);

            // Add one more filler todo to grow the object count for the next iteration
            if (objectCount < MAX_OBJECTS) {
                addFillerTodo();
            }
        }

        CsvResultWriter.write("update_results.csv", results);
        System.out.println("[UpdateTest] Experiment complete. " + results.size() + " data points recorded.");
    }

    private static void createTargetTodo() {
        Payloads.TodoPayload target = TodoApiHelper.buildTodo(
                "target todo for update experiment",
                "This todo is repeatedly updated during the performance test.",
                false);
        Response response = TodoApiHelper.createTodo(target);
        if (response.getStatusCode() != 201) {
            throw new RuntimeException("Failed to create target todo: " + response.getStatusCode());
        }
        targetTodoId = response.jsonPath().getString("id");
        allCreatedIds.add(targetTodoId);
    }

    private static void addFillerTodo() {
        Payloads.TodoPayload filler = RandomDataGenerator.randomTodo();
        Response response = TodoApiHelper.createTodo(filler);
        if (response.getStatusCode() != 201) {
            throw new RuntimeException("Failed to create filler todo: " + response.getStatusCode());
        }
        allCreatedIds.add(response.jsonPath().getString("id"));
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
