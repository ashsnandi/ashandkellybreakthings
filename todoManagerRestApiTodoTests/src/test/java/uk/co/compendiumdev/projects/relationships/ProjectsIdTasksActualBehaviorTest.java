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
 * Actual behavior tests for GET /projects/:id/tasks.
 */
@TestMethodOrder(MethodOrderer.Random.class)
@ExtendWith(uk.co.compendiumdev.todos.helpers.TestNameLogger.class)
class ProjectsIdTasksActualBehaviorTest {

    private Map<String, Payloads.ProjectPayload> savedProjects;

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
        Response response = TodoApiHelper.getAllProjects();
        Payloads.ProjectsPayload projects = response.body().as(Payloads.ProjectsPayload.class);
        if (projects.projects != null) {
            for (Payloads.ProjectPayload project : projects.projects) {
                savedProjects.put(project.id, project);
            }
        }
    }

    @AfterEach
    void restoreState() {
        Response response = TodoApiHelper.getAllProjects();
        Payloads.ProjectsPayload projects = response.body().as(Payloads.ProjectsPayload.class);
        if (projects.projects != null) {
            for (Payloads.ProjectPayload project : projects.projects) {
                if (!savedProjects.containsKey(project.id)) {
                    TodoApiHelper.deleteProject(project.id);
                }
            }
        }
    }

    @Test
    void getProjectTasksDeletedProjectReturns200AndEmptyListActual() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "actual delete tasks", "delete tasks", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        TodoApiHelper.deleteProject(projectId);

        Response response = TodoApiHelper.getProjectTasks(projectId);

        Assertions.assertEquals(200, response.getStatusCode());

        Payloads.TodosPayload todos = response.body().as(Payloads.TodosPayload.class);
        Assertions.assertTrue(todos.todos == null || todos.todos.isEmpty());
    }

    @Test
    void getProjectTasksNeverExistedProjectReturns200AndEmptyListActual() {
        Response response = TodoApiHelper.getProjectTasks("99999");

        Assertions.assertEquals(200, response.getStatusCode());

        Payloads.TodosPayload todos = response.body().as(Payloads.TodosPayload.class);
        Assertions.assertTrue(todos.todos == null || todos.todos.isEmpty());
    }
}
