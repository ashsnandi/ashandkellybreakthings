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
 * Tests for the /todos/:id endpoint (JSON).
 * Covers: GET /todos/:id, POST /todos/:id (amend), PUT /todos/:id (update), DELETE /todos/:id
 */
@TestMethodOrder(MethodOrderer.Random.class)
class TodosIdEndpointTest {

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

    // GET /todos/:id 

    @Test
    void getTodoByIdReturnsExistingTodo() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "get by id test", "test description", true);
        Response createResponse = TodoApiHelper.createTodo(newTodo);
        String createdId = createResponse.body().as(Payloads.TodoPayload.class).id;

        Response response = TodoApiHelper.getTodoById(createdId);

        Assertions.assertEquals(200, response.getStatusCode());

        Payloads.TodoPayload retrieved = response.body()
                .as(Payloads.TodosPayload.class).todos.get(0);
        Assertions.assertEquals(createdId, retrieved.id);
        Assertions.assertEquals("get by id test", retrieved.title);
        Assertions.assertEquals("test description", retrieved.description);
        Assertions.assertEquals(true, retrieved.doneStatus);
    }

    @Test
    void getTodoByIdNonExistentReturns404() {
        Response response = TodoApiHelper.getTodoById("99999");

        Assertions.assertEquals(404, response.getStatusCode());

        Payloads.ErrorMessageResponse errors =
                response.body().as(Payloads.ErrorMessageResponse.class);
        Assertions.assertFalse(errors.errorMessages.isEmpty());
    }

    // POST /todos/:id (amend) 

    @Test
    void amendTodoWithAllFields() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "before amend", "before desc", false);
        Response createResponse = TodoApiHelper.createTodo(newTodo);
        String createdId = createResponse.body().as(Payloads.TodoPayload.class).id;

        Payloads.TodoPayload amendPayload = TodoApiHelper.buildTodo(
                "after amend", "after desc", true);
        Response amendResponse = TodoApiHelper.amendTodo(createdId, amendPayload);

        Assertions.assertEquals(200, amendResponse.getStatusCode());

        Payloads.TodoPayload amended = amendResponse.body().as(Payloads.TodoPayload.class);
        Assertions.assertEquals("after amend", amended.title);
        Assertions.assertEquals("after desc", amended.description);
        Assertions.assertEquals(true, amended.doneStatus);
    }

    @Test
    void amendTodoNonExistentIdReturns404() {
        Payloads.TodoPayload amendPayload = TodoApiHelper.buildTodo(
                "nonexistent", "", false);
        Response response = TodoApiHelper.amendTodo("99999", amendPayload);

        Assertions.assertEquals(404, response.getStatusCode());
    }

    //  PUT /todos/:id (update) 

    @Test
    void updateTodoWithAllFields() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "before update", "before desc", false);
        Response createResponse = TodoApiHelper.createTodo(newTodo);
        String createdId = createResponse.body().as(Payloads.TodoPayload.class).id;

        Payloads.TodoPayload updatePayload = TodoApiHelper.buildTodo(
                "after update", "after desc", true);
        Response updateResponse = TodoApiHelper.updateTodo(createdId, updatePayload);

        Assertions.assertEquals(200, updateResponse.getStatusCode());

        Payloads.TodoPayload updated = updateResponse.body().as(Payloads.TodoPayload.class);
        Assertions.assertEquals("after update", updated.title);
        Assertions.assertEquals("after desc", updated.description);
        Assertions.assertEquals(true, updated.doneStatus);
    }

    @Test
    void updateTodoNonExistentIdReturns404() {
        Payloads.TodoPayload updatePayload = TodoApiHelper.buildTodo(
                "nonexistent", "", false);
        Response response = TodoApiHelper.updateTodo("99999", updatePayload);

        Assertions.assertEquals(404, response.getStatusCode());
    }

    // DELETE /todos/:id 

    @Test
    void deleteTodoExistingReturns200() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "delete me", "to be deleted", false);
        Response createResponse = TodoApiHelper.createTodo(newTodo);
        String createdId = createResponse.body().as(Payloads.TodoPayload.class).id;

        Response deleteResponse = TodoApiHelper.deleteTodo(createdId);
        Assertions.assertEquals(200, deleteResponse.getStatusCode());

        Response getResponse = TodoApiHelper.getTodoById(createdId);
        Assertions.assertEquals(404, getResponse.getStatusCode());
    }

    @Test
    void deleteTodoNonExistentIdReturns404() {
        Response response = TodoApiHelper.deleteTodo("99999");
        Assertions.assertEquals(404, response.getStatusCode());
    }
}