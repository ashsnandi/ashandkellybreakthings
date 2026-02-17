package uk.co.compendiumdev.projects.relationships;

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
import org.junit.jupiter.api.extension.ExtendWith;

import io.restassured.response.Response;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.sparkstart.Port;
import uk.co.compendiumdev.todos.helpers.Payloads;
import uk.co.compendiumdev.todos.helpers.TodoApiHelper;

/**
 * Tests for POST /projects/:id/tasks (JSON + XML).
 */
@TestMethodOrder(MethodOrderer.Random.class)
@ExtendWith(uk.co.compendiumdev.todos.helpers.TestNameLogger.class)
class ProjectsIdTasksCreateEndpointTest {

    private Map<String, Payloads.ProjectPayload> savedProjects;
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
        savedProjects = new HashMap<>();
        Response projectResponse = TodoApiHelper.getAllProjects();
        Payloads.ProjectsPayload projects = projectResponse.body().as(Payloads.ProjectsPayload.class);
        if (projects.projects != null) {
            for (Payloads.ProjectPayload project : projects.projects) {
                savedProjects.put(project.id, project);
            }
        }

        savedTodos = new HashMap<>();
        Response todoResponse = TodoApiHelper.getAllTodos();
        Payloads.TodosPayload todos = todoResponse.body().as(Payloads.TodosPayload.class);
        if (todos.todos != null) {
            for (Payloads.TodoPayload todo : todos.todos) {
                savedTodos.put(todo.id, todo);
            }
        }
    }

    @AfterEach
    void restoreState() {
        Response projectResponse = TodoApiHelper.getAllProjects();
        Payloads.ProjectsPayload projects = projectResponse.body().as(Payloads.ProjectsPayload.class);
        if (projects.projects != null) {
            for (Payloads.ProjectPayload project : projects.projects) {
                if (!savedProjects.containsKey(project.id)) {
                    TodoApiHelper.deleteProject(project.id);
                }
            }
        }

        Response todoResponse = TodoApiHelper.getAllTodos();
        Payloads.TodosPayload todos = todoResponse.body().as(Payloads.TodosPayload.class);
        if (todos.todos != null) {
            for (Payloads.TodoPayload todo : todos.todos) {
                if (!savedTodos.containsKey(todo.id)) {
                    TodoApiHelper.deleteTodo(todo.id);
                }
            }
        }
    }

    @Test
    void createTaskUnderProjectValidReturns201Or200AndTaskRetrievable() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "task create", "create task", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        Payloads.TodoPayload todoPayload = TodoApiHelper.buildTodo(
                "task title", "task description", false);
        Response response = TodoApiHelper.createTaskUnderProject(projectId, todoPayload);

        Assertions.assertTrue(
                response.getStatusCode() == 201 || response.getStatusCode() == 200);

        String todoId = response.body().as(Payloads.TodoPayload.class).id;
        Assertions.assertNotNull(todoId);

        Response getResponse = TodoApiHelper.getTodoById(todoId);
        Assertions.assertEquals(200, getResponse.getStatusCode());
    }

    @Test
    void createTaskUnderProjectWithIdProvidedReturns4xxNotAllowedToCreateWithId() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "task id", "with id", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        String rawJson = "{\"id\":\"123\",\"title\":\"task with id\"}";
        Response response = TodoApiHelper.createTaskUnderProjectWithRawJson(projectId, rawJson);

        Assertions.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500);
    }

    @Test
    void createTaskUnderProjectMissingTitleReturns4xxTitleMandatory() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "task missing title", "missing title", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        Payloads.TodoPayload todoPayload = new Payloads.TodoPayload();
        todoPayload.description = "missing title";

        Response response = TodoApiHelper.createTaskUnderProject(projectId, todoPayload);

        Assertions.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500);
    }

    @Test
    void createTaskUnderProjectNonexistentProjectReturns4xxOr404() {
        Payloads.TodoPayload todoPayload = TodoApiHelper.buildTodo(
                "task missing parent", "missing parent", false);

        Response response = TodoApiHelper.createTaskUnderProject("99999", todoPayload);

        Assertions.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500);
    }

    @Test
    void createTaskUnderProjectDeletedProjectReturns4xxOr404() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "task deleted parent", "deleted parent", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        TodoApiHelper.deleteProject(projectId);

        Payloads.TodoPayload todoPayload = TodoApiHelper.buildTodo(
                "task missing parent", "missing parent", false);
        Response response = TodoApiHelper.createTaskUnderProject(projectId, todoPayload);

        Assertions.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500);
    }

    @Test
    void createTaskUnderProjectMalformedJsonReturns4xx() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "task malformed", "malformed", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        String rawJson = "{title: broken json, missing quotes";
        Response response = TodoApiHelper.createTaskUnderProjectWithRawJson(projectId, rawJson);

        Assertions.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500);
    }

    @Test
    void createTaskUnderProjectAcceptXmlOrXmlSupportCheck() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "task xml", "xml", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        String xmlBody = "<todo>"
                + "<title>xml task title</title>"
                + "<description>xml task</description>"
                + "</todo>";

        Response response = TodoApiHelper.createTaskUnderProjectWithRawXml(projectId, xmlBody);

        Assertions.assertTrue(
                response.getStatusCode() == 201 || response.getStatusCode() == 200);
        Assertions.assertTrue(
                response.getContentType().contains("application/xml"));
    }
}
