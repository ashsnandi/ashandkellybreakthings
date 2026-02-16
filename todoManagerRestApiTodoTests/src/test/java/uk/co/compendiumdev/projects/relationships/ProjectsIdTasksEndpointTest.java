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

import io.restassured.response.Response;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.sparkstart.Port;
import uk.co.compendiumdev.todos.helpers.Payloads;
import uk.co.compendiumdev.todos.helpers.TodoApiHelper;

/**
 * Tests for GET /projects/:id/tasks (JSON + XML).
 */
@TestMethodOrder(MethodOrderer.Random.class)
class ProjectsIdTasksEndpointTest {

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
    void getProjectTasksExistingProjectReturns200AndMatchesTasksFromProjectRepresentation() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "tasks project", "for tasks", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        Payloads.TodoPayload todoPayload = TodoApiHelper.buildTodo(
                "task todo", "linked", false);
        Response createTask = TodoApiHelper.createTaskUnderProject(projectId, todoPayload);

        Assertions.assertTrue(
                createTask.getStatusCode() == 201 || createTask.getStatusCode() == 200);

        String createdTodoId = createTask.body().as(Payloads.TodoPayload.class).id;

        Response projectResponse = TodoApiHelper.getProjectById(projectId);
        Payloads.ProjectPayload project = projectResponse.body()
                .as(Payloads.ProjectsPayload.class).projects.get(0);

        Assertions.assertNotNull(project.tasks);
        Assertions.assertTrue(
                project.tasks.stream().anyMatch(task -> createdTodoId.equals(task.id)));

        Response tasksResponse = TodoApiHelper.getProjectTasks(projectId);

        Assertions.assertEquals(200, tasksResponse.getStatusCode());

        Payloads.TodosPayload todos = tasksResponse.body().as(Payloads.TodosPayload.class);
        Assertions.assertTrue(
                todos.todos.stream().anyMatch(todo -> createdTodoId.equals(todo.id)));
    }

    @Test
    void getProjectTasksAcceptXmlReturnsXml() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "tasks xml", "xml tasks", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        Payloads.TodoPayload todoPayload = TodoApiHelper.buildTodo(
                "xml task todo", "linked", false);
        TodoApiHelper.createTaskUnderProject(projectId, todoPayload);

        Response response = TodoApiHelper.getProjectTasksAsXml(projectId);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(
                response.getContentType().contains("application/xml"));

        String title = response.xmlPath().getString("todos.todo[0].title");
        Assertions.assertNotNull(title);
    }
}
