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
 * Performance experiment: measures how long it takes to CREATE a todo
 * as the total number of objects in the system grows from 0 to MAX_OBJECTS.
 *
 * Experiment:
 *   - Iteration i: there are already i todos in the system.
 *   - We create one more and record the time taken.
 *   - After the loop, MAX_OBJECTS todos exist; we delete all of them.
 *
 * Output: performance_results/create_results.csv
 *   Columns: objectCount, operationTimeMs, heapUsedMB, heapMaxMB
 */
public class TodoCreatePerformanceTest {

    private static final int MAX_OBJECTS = 500;
    private static final List<String> createdIds = new ArrayList<>();

    @BeforeAll
    static void ensureServiceIsRunning() {
        boolean serverUp = uk.co.compendiumdev.sparkstart.Port.inUse("localhost", 4567);
        if (!serverUp) {
            // Attempt auto-start via Environment; throws if JAR not found
            Environment.getBaseUri();
        }
    }

    @AfterAll
    static void deleteAllCreatedTodos() {
        System.out.println("[CreateTest] Cleaning up " + createdIds.size() + " todos...");
        for (String id : createdIds) {
            TodoApiHelper.deleteTodo(id);
        }
        createdIds.clear();
        System.out.println("[CreateTest] Cleanup complete.");
    }

    @Test
    void measureCreateTimeAsObjectCountGrows() {
        List<PerformanceResult> results = new ArrayList<>();

        for (int i = 0; i < MAX_OBJECTS; i++) {
            Payloads.TodoPayload todo = RandomDataGenerator.randomTodo();

            long startNs = System.nanoTime();
            Response response = TodoApiHelper.createTodo(todo);
            long endNs = System.nanoTime();

            if (response.getStatusCode() != 201) {
                fail("Expected 201 Created at objectCount=" + i
                        + " but got " + response.getStatusCode());
            }

            String id = response.jsonPath().getString("id");
            createdIds.add(id);

            double elapsedMs = nanosToMillis(endNs - startNs);
            long heapUsed = SystemMonitor.getHeapUsedMB();
            long heapMax = SystemMonitor.getHeapMaxMB();

            results.add(new PerformanceResult(i, elapsedMs, heapUsed, heapMax));
            printProgress("CREATE", i, elapsedMs, heapUsed);
        }

        CsvResultWriter.write("create_results.csv", results);
        System.out.println("[CreateTest] Experiment complete. " + results.size() + " data points recorded.");
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
