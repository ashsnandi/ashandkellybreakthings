package uk.co.compendiumdev.todos;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.sparkstart.Port;
import uk.co.compendiumdev.todos.helpers.Payloads;
import uk.co.compendiumdev.todos.helpers.TodoApiHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for the /todos collection endpoint (XML).
 * Covers: GET /todos (XML), POST /todos (XML), GET /todos?title=X (XML)
 */
@TestMethodOrder(MethodOrderer.Random.class)
class TodosEndpointXmlTest {

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

    // ---------- GET /todos (XML) ----------

    @Test
    void getAllTodosAsXmlReturnsXmlContentType() {
        Response response = TodoApiHelper.getAllTodosAsXml();

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(
                response.getContentType().contains("application/xml"),
                "Response content type should be XML");
    }

    @Test
    void getAllTodosAsXmlContainsDefaultTodos() {
        Response response = TodoApiHelper.getAllTodosAsXml();

        Assertions.assertEquals(200, response.getStatusCode());

        String firstTitle = response.xmlPath().getString("todos.todo[0].title");
        Assertions.assertNotNull(firstTitle, "Should contain at least one todo title");
    }

    // ---------- POST /todos (XML) ----------

    @Test
    void createTodoWithXmlPayloadReturns201() {
        String xmlBody = "<todo>"
                + "<title>XML Created Todo</title>"
                + "<doneStatus>false</doneStatus>"
                + "<description>Created via XML</description>"
                + "</todo>";

        Response response = TodoApiHelper.createTodoWithRawXml(xmlBody);

        Assertions.assertEquals(201, response.getStatusCode());

        String title = response.xmlPath().getString("todo.title");
        Assertions.assertEquals("XML Created Todo", title);

        String description = response.xmlPath().getString("todo.description");
        Assertions.assertEquals("Created via XML", description);
    }

    @Test
    void createTodoWithXmlTitleOnlyAppliesDefaults() {
        String xmlBody = "<todo>"
                + "<title>XML Title Only</title>"
                + "</todo>";

        Response response = TodoApiHelper.createTodoWithRawXml(xmlBody);

        Assertions.assertEquals(201, response.getStatusCode());

        String title = response.xmlPath().getString("todo.title");
        Assertions.assertEquals("XML Title Only", title);

        String doneStatus = response.xmlPath().getString("todo.doneStatus");
        Assertions.assertEquals("false", doneStatus);
    }

    @Test
    void createTodoWithXmlWithoutTitleReturns400() {
        String xmlBody = "<todo>"
                + "<description>no title</description>"
                + "</todo>";

        Response response = TodoApiHelper.createTodoWithRawXml(xmlBody);

        Assertions.assertEquals(400, response.getStatusCode());
    }

    @Test
    void createTodoWithMalformedXmlReturns400() {
        String malformedXml = "<todo><title>broken xml";

        Response response = TodoApiHelper.createTodoWithRawXml(malformedXml);

        Assertions.assertEquals(400, response.getStatusCode());
    }

    // ---------- GET /todos?title=X (XML) ----------

    @Test
    void filterTodosByTitleAsXml() {
        String xmlBody = "<todo>"
                + "<title>xml filter target</title>"
                + "<doneStatus>false</doneStatus>"
                + "<description>for xml filtering</description>"
                + "</todo>";
        TodoApiHelper.createTodoWithRawXml(xmlBody);

        Response response = TodoApiHelper.getTodosByTitleAsXml("xml filter target");

        Assertions.assertEquals(200, response.getStatusCode());

        String title = response.xmlPath().getString("todos.todo[0].title");
        Assertions.assertEquals("xml filter target", title);
    }
}