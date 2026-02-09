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

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.sparkstart.Port;
import uk.co.compendiumdev.todos.helpers.Payloads;
import uk.co.compendiumdev.todos.helpers.TodoApiHelper;

/**
 * Tests for the /todos collection endpoint (JSON).
 * Covers: GET /todos, HEAD /todos, POST /todos, GET /todos?title=X
 */
@TestMethodOrder(MethodOrderer.Random.class)
class TodosEndpointTest {

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
                }
            }
        }
    }

    // GET /todos

    @Test
    void getAllTodosReturnsAllInstances() {
        Response response = TodoApiHelper.getAllTodos();

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(
                response.getContentType().contains(ContentType.JSON.toString()));

        Payloads.TodosPayload todos = response.body().as(Payloads.TodosPayload.class);
        Assertions.assertNotNull(todos.todos);
        Assertions.assertTrue(todos.todos.size() >= 2,
                "Should contain at least the two default todos");
    }

    @Test
    void getAllTodosDoesNotModifyData() {
        Response before = TodoApiHelper.getAllTodos();
        int countBefore = before.body().as(Payloads.TodosPayload.class).todos.size();

        TodoApiHelper.getAllTodos();

        Response after = TodoApiHelper.getAllTodos();
        int countAfter = after.body().as(Payloads.TodosPayload.class).todos.size();

        Assertions.assertEquals(countBefore, countAfter);
    }

    // HEAD /todos 

    @Test
    void headTodosReturnsHeadersAndEmptyBody() {
        Response response = TodoApiHelper.headTodos();

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertNotNull(response.getHeader("Content-Type"));
        Assertions.assertTrue(response.getBody().asString().isEmpty(),
                "HEAD response body should be empty");
    }

    // POST /todos 

    @Test
    void createTodoWithAllFields() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "full todo", "full description", true);

        Response response = TodoApiHelper.createTodo(newTodo);

        Assertions.assertEquals(201, response.getStatusCode());

        Payloads.TodoPayload created = response.body().as(Payloads.TodoPayload.class);
        Assertions.assertNotNull(created.id);
        Assertions.assertEquals("full todo", created.title);
        Assertions.assertEquals("full description", created.description);
        Assertions.assertEquals(true, created.doneStatus);
    }

    @Test
    void createTodoWithTitleOnlyAppliesDefaults() {
        Payloads.TodoPayload newTodo = new Payloads.TodoPayload();
        newTodo.title = "title only todo";

        Response response = TodoApiHelper.createTodo(newTodo);

        Assertions.assertEquals(201, response.getStatusCode());

        Payloads.TodoPayload created = response.body().as(Payloads.TodoPayload.class);
        Assertions.assertEquals("title only todo", created.title);
        Assertions.assertEquals(false, created.doneStatus);
        Assertions.assertEquals("", created.description);
    }

    @Test
    void createTodoWithoutMandatoryTitleReturns400() {
        Payloads.TodoPayload newTodo = new Payloads.TodoPayload();
        newTodo.description = "no title";
        newTodo.doneStatus = false;

        Response response = TodoApiHelper.createTodo(newTodo);

        Assertions.assertEquals(400, response.getStatusCode());

        Payloads.ErrorMessageResponse errors =
                response.body().as(Payloads.ErrorMessageResponse.class);
        Assertions.assertEquals("title : field is mandatory",
                errors.errorMessages.get(0));
    }

    @Test
    void createTodoWithEmptyTitleReturns400() {
        Payloads.TodoPayload newTodo = new Payloads.TodoPayload();
        newTodo.title = "";

        Response response = TodoApiHelper.createTodo(newTodo);

        Assertions.assertEquals(400, response.getStatusCode());
    }

    @Test
    void createTodoWithInvalidDoneStatusReturns400() {
        String rawJson = "{\"title\":\"bad status\",\"doneStatus\":\"notABoolean\"}";

        Response response = TodoApiHelper.createTodoWithRawJson(rawJson);

        Assertions.assertEquals(400, response.getStatusCode());
    }

    @Test
    void createTodoWithMalformedJsonReturns400() {
        String malformedJson = "{title: broken json, missing quotes";

        Response response = TodoApiHelper.createTodoWithRawJson(malformedJson);

        Assertions.assertEquals(400, response.getStatusCode());
    }

    // GET /todos?title=X 

    @Test
    void filterTodosByExactTitle() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "unique filter title", "for filtering", false);
        TodoApiHelper.createTodo(newTodo);

        Response response = TodoApiHelper.getTodosByTitle("unique filter title");

        Assertions.assertEquals(200, response.getStatusCode());

        Payloads.TodosPayload filtered = response.body().as(Payloads.TodosPayload.class);
        Assertions.assertNotNull(filtered.todos);
        Assertions.assertEquals(1, filtered.todos.size());
        Assertions.assertEquals("unique filter title", filtered.todos.get(0).title);
    }
}