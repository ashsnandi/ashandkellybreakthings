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
 * Tests for the /todos/:id/tasksof relationship endpoint (XML).
 * Covers: GET /todos/:id/tasksof (Accept: XML)
 */
@TestMethodOrder(MethodOrderer.Random.class)
class TodosIdTasksofEndpointXmlTest {

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

    // ---------- GET /todos/:id/tasksof (XML) ----------

    @Test
    void getTasksOfLinkedTodoAsXml() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "xml linked todo", "linked to project", false);
        Response createTodoResponse = TodoApiHelper.createTodo(newTodo);
        String todoId = createTodoResponse.body().as(Payloads.TodoPayload.class).id;

        Payloads.ProjectPayload project = new Payloads.ProjectPayload();
        project.title = "xml test project";
        Response createProjectResponse = TodoApiHelper.createProject(project);
        String projectId = createProjectResponse.body().as(Payloads.ProjectPayload.class).id;

        TodoApiHelper.linkTodoToProject(projectId, todoId);

        Response tasksOfResponse = TodoApiHelper.getTasksOfTodoAsXml(todoId);

        Assertions.assertEquals(200, tasksOfResponse.getStatusCode());
        Assertions.assertTrue(
                tasksOfResponse.getContentType().contains("application/xml"));

        String projectTitle = tasksOfResponse.xmlPath().getString("projects.project[0].title");
        Assertions.assertNotNull(projectTitle, "Should return at least one linked project");
    }

    @Test
    void getTasksOfUnlinkedTodoAsXmlReturnsEmpty() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "xml unlinked todo", "", false);
        Response createResponse = TodoApiHelper.createTodo(newTodo);
        String todoId = createResponse.body().as(Payloads.TodoPayload.class).id;

        Response tasksOfResponse = TodoApiHelper.getTasksOfTodoAsXml(todoId);

        Assertions.assertEquals(200, tasksOfResponse.getStatusCode());

        String projectTitle = tasksOfResponse.xmlPath().getString("projects.project[0].title");
        Assertions.assertTrue(
                projectTitle == null || projectTitle.isEmpty(),
                "Unlinked todo should have no associated projects in XML response");
    }

    @Test
    void getTasksOfNonExistentTodoAsXmlReturns200() {
        Response response = TodoApiHelper.getTasksOfTodoAsXml("99999");
        Assertions.assertEquals(200, response.getStatusCode());
    }
}