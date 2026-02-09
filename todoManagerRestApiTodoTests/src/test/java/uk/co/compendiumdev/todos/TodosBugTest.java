package uk.co.compendiumdev.todos;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.restassured.response.Response;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.sparkstart.Port;
import uk.co.compendiumdev.todos.helpers.Payloads;
import uk.co.compendiumdev.todos.helpers.TodoApiHelper;

/**
 * Tests for PUT /todos/:id full replacement behavior (JSON).
 * PUT replaces the entire resource, so omitted fields reset to defaults.
 */
@TestMethodOrder(MethodOrderer.Random.class)
class TodosBugTest {

    private Map<String, Payloads.TodoPayload> savedTodos;

    @BeforeAll
    static void startServer() {
        Environment.getBaseUri();
        Assumptions.assumeTrue(
                Port.inUse("localhost", 4567),
                "Server is not running on localhost:4567");
    }

    @BeforeEach
    void saveState() {
        savedTodos = new HashMap<>();
        Response response = TodoApiHelper.getAllTodos();
        Payloads.TodosPayload todos = response.body().as(Payloads.TodosPayload.class);
        if (todos.todos != null) {
            for (Payloads.TodoPayload todo : todos.todos) {
                savedTodos.put(todo.id, todo);
            }
        }
    }

    @AfterEach
    void restoreState() {
        Response response = TodoApiHelper.getAllTodos();
        Payloads.TodosPayload todos = response.body().as(Payloads.TodosPayload.class);
        if (todos.todos != null) {
            for (Payloads.TodoPayload todo : todos.todos) {
                if (!savedTodos.containsKey(todo.id)) {
                    TodoApiHelper.deleteTodo(todo.id);
                } else {
                    Payloads.TodoPayload original = savedTodos.get(todo.id);
                    Payloads.TodoPayload restore = TodoApiHelper.buildTodo(
                            original.title, original.description, original.doneStatus);
                    TodoApiHelper.amendTodo(todo.id, restore);
                }
            }
        }
    }

    // PUT /todos/:id full replacement behavior

    @Test
    void putWithOnlyTitleResetsOmittedFieldsToDefaults() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "wipe test", "this will be wiped", true);
        Response createResponse = TodoApiHelper.createTodo(newTodo);
        String createdId = createResponse.body().as(Payloads.TodoPayload.class).id;

        Payloads.TodoPayload updatePayload = new Payloads.TodoPayload();
        updatePayload.title = "updated title";

        TodoApiHelper.updateTodo(createdId, updatePayload);

        Response getResponse = TodoApiHelper.getTodoById(createdId);
        Payloads.TodoPayload retrieved = getResponse.body()
                .as(Payloads.TodosPayload.class).todos.get(0);

        // ACTUAL: omitted fields are wiped to defaults
        Assertions.assertEquals("", retrieved.description,
                "PUT with only title wipes description to empty string");
        Assertions.assertEquals(false, retrieved.doneStatus,
                "PUT with only title resets doneStatus to false");
    }

    @Test
    void putWithAllFieldsPreservesAll() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "full put test", "keep everything", true);
        Response createResponse = TodoApiHelper.createTodo(newTodo);
        String createdId = createResponse.body().as(Payloads.TodoPayload.class).id;

        Payloads.TodoPayload updatePayload = TodoApiHelper.buildTodo(
                "updated title", "keep everything", true);

        TodoApiHelper.updateTodo(createdId, updatePayload);

        Response getResponse = TodoApiHelper.getTodoById(createdId);
        Payloads.TodoPayload retrieved = getResponse.body()
                .as(Payloads.TodosPayload.class).todos.get(0);

        Assertions.assertEquals("updated title", retrieved.title);
        Assertions.assertEquals("keep everything", retrieved.description);
        Assertions.assertEquals(true, retrieved.doneStatus);
    }
}