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
 * Tests for the /todos/:id endpoint (XML).
 * Covers: GET /todos/:id (XML), POST /todos/:id (XML amend),
 *         PUT /todos/:id (XML update), DELETE /todos/:id
 */
@TestMethodOrder(MethodOrderer.Random.class)
class TodosIdEndpointXmlTest {

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

    // GET /todos/:id (XML)

    @Test
    void getTodoByIdAsXmlReturnsAllFields() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "xml get test", "xml description", true);
        Response createResponse = TodoApiHelper.createTodo(newTodo);
        String createdId = createResponse.body().as(Payloads.TodoPayload.class).id;

        Response response = TodoApiHelper.getTodoByIdAsXml(createdId);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(
                response.getContentType().contains("application/xml"));

        String title = response.xmlPath().getString("todos.todo[0].title");
        String description = response.xmlPath().getString("todos.todo[0].description");
        String doneStatus = response.xmlPath().getString("todos.todo[0].doneStatus");

        Assertions.assertEquals("xml get test", title);
        Assertions.assertEquals("xml description", description);
        Assertions.assertEquals("true", doneStatus);
    }

    @Test
    void getTodoByIdAsXmlNonExistentReturns404() {
        Response response = TodoApiHelper.getTodoByIdAsXml("99999");

        Assertions.assertEquals(404, response.getStatusCode());
    }

    // POST /todos/:id (XML amend)

    @Test
    void amendTodoWithXmlPayload() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "xml amend before", "before desc", false);
        Response createResponse = TodoApiHelper.createTodo(newTodo);
        String createdId = createResponse.body().as(Payloads.TodoPayload.class).id;

        String xmlBody = "<todo>"
                + "<title>xml amend after</title>"
                + "<description>after desc</description>"
                + "<doneStatus>true</doneStatus>"
                + "</todo>";

        Response amendResponse = TodoApiHelper.amendTodoWithXml(createdId, xmlBody);

        Assertions.assertEquals(200, amendResponse.getStatusCode());

        String title = amendResponse.xmlPath().getString("todo.title");
        Assertions.assertEquals("xml amend after", title);

        String description = amendResponse.xmlPath().getString("todo.description");
        Assertions.assertEquals("after desc", description);
    }

    @Test
    void amendTodoWithXmlNonExistentIdReturns404() {
        String xmlBody = "<todo>"
                + "<title>nonexistent</title>"
                + "</todo>";

        Response response = TodoApiHelper.amendTodoWithXml("99999", xmlBody);

        Assertions.assertEquals(404, response.getStatusCode());
    }

    // PUT /todos/:id (XML update)

    @Test
    void updateTodoWithXmlPayload() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "xml update before", "before desc", false);
        Response createResponse = TodoApiHelper.createTodo(newTodo);
        String createdId = createResponse.body().as(Payloads.TodoPayload.class).id;

        String xmlBody = "<todo>"
                + "<title>xml update after</title>"
                + "<description>after desc</description>"
                + "<doneStatus>true</doneStatus>"
                + "</todo>";

        Response updateResponse = TodoApiHelper.updateTodoWithXml(createdId, xmlBody);

        Assertions.assertEquals(200, updateResponse.getStatusCode());

        String title = updateResponse.xmlPath().getString("todo.title");
        Assertions.assertEquals("xml update after", title);
    }

    @Test
    void updateTodoWithXmlNonExistentIdReturns404() {
        String xmlBody = "<todo>"
                + "<title>nonexistent</title>"
                + "</todo>";

        Response response = TodoApiHelper.updateTodoWithXml("99999", xmlBody);

        Assertions.assertEquals(404, response.getStatusCode());
    }

    // DELETE /todos/:id

    @Test
    void deleteTodoAndVerifyGoneViaXml() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "xml delete test", "to be deleted", false);
        Response createResponse = TodoApiHelper.createTodo(newTodo);
        String createdId = createResponse.body().as(Payloads.TodoPayload.class).id;

        Response deleteResponse = TodoApiHelper.deleteTodo(createdId);
        Assertions.assertEquals(200, deleteResponse.getStatusCode());

        Response getResponse = TodoApiHelper.getTodoByIdAsXml(createdId);
        Assertions.assertEquals(404, getResponse.getStatusCode());
    }

    @Test
    void deleteTodoNonExistentIdReturns404() {
        Response response = TodoApiHelper.deleteTodo("99999");
        Assertions.assertEquals(404, response.getStatusCode());
    }
}