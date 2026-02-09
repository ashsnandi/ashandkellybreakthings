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
 * Tests for PUT /todos/:id full replacement behavior (XML).
 * PUT replaces the entire resource, so omitted fields reset to defaults.
 */
@TestMethodOrder(MethodOrderer.Random.class)
class TodosBugXmlTest {

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

    // PUT /todos/:id full replacement behavior (XML) 

    @Test
    void xmlPutWithOnlyTitleResetsOmittedFieldsToDefaults() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "xml bug2 actual", "this will be wiped", true);
        Response createResponse = TodoApiHelper.createTodo(newTodo);
        String createdId = createResponse.body().as(Payloads.TodoPayload.class).id;

        String xmlBody = "<todo>"
                + "<title>xml updated title</title>"
                + "</todo>";

        TodoApiHelper.updateTodoWithXml(createdId, xmlBody);

        Response getResponse = TodoApiHelper.getTodoByIdAsXml(createdId);

        String description = getResponse.xmlPath().getString("todos.todo[0].description");
        String doneStatus = getResponse.xmlPath().getString("todos.todo[0].doneStatus");

        // ACTUAL: omitted fields are wiped to defaults
        Assertions.assertEquals("", description,
                "PUT with only title wipes description to empty string");
        Assertions.assertEquals("false", doneStatus,
                "PUT with only title resets doneStatus to false");
    }

}