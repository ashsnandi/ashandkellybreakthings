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
 * Tests for the /todos/:id/tasksof relationship endpoint (JSON).
 * Covers: GET /todos/:id/tasksof
 */
@TestMethodOrder(MethodOrderer.Random.class)
class TodosIdTasksofEndpointTest {

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

    // ---------- GET /todos/:id/tasksof ----------

    @Test
    void getTasksOfLinkedTodoReturnsProjects() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "linked todo", "linked to project", false);
        Response createTodoResponse = TodoApiHelper.createTodo(newTodo);
        String todoId = createTodoResponse.body().as(Payloads.TodoPayload.class).id;

        Payloads.ProjectPayload project = new Payloads.ProjectPayload();
        project.title = "test project";
        Response createProjectResponse = TodoApiHelper.createProject(project);
        String projectId = createProjectResponse.body().as(Payloads.ProjectPayload.class).id;

        TodoApiHelper.linkTodoToProject(projectId, todoId);

        Response tasksOfResponse = TodoApiHelper.getTasksOfTodo(todoId);

        Assertions.assertEquals(200, tasksOfResponse.getStatusCode());

        Payloads.ProjectsPayload projects =
                tasksOfResponse.body().as(Payloads.ProjectsPayload.class);
        Assertions.assertNotNull(projects.projects);
        Assertions.assertTrue(projects.projects.size() >= 1,
                "Should return at least one linked project");
    }

    @Test
    void getTasksOfUnlinkedTodoReturnsEmpty() {
        Payloads.TodoPayload newTodo = TodoApiHelper.buildTodo(
                "unlinked todo", "", false);
        Response createResponse = TodoApiHelper.createTodo(newTodo);
        String todoId = createResponse.body().as(Payloads.TodoPayload.class).id;

        Response tasksOfResponse = TodoApiHelper.getTasksOfTodo(todoId);

        Assertions.assertEquals(200, tasksOfResponse.getStatusCode());

        Payloads.ProjectsPayload projects =
                tasksOfResponse.body().as(Payloads.ProjectsPayload.class);
        Assertions.assertTrue(
                projects.projects == null || projects.projects.isEmpty(),
                "Unlinked todo should have no associated projects");
    }

    @Test
    void getTasksOfNonExistentTodoReturns200() {
        Response response = TodoApiHelper.getTasksOfTodo("99999");
        Assertions.assertEquals(200, response.getStatusCode());
    }
}