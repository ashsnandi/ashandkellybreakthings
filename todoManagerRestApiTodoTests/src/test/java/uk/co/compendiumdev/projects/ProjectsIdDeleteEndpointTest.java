package uk.co.compendiumdev.projects;

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
 * Tests for DELETE /projects/:id (JSON).
 */
@TestMethodOrder(MethodOrderer.Random.class)
@ExtendWith(uk.co.compendiumdev.todos.helpers.TestNameLogger.class)
class ProjectsIdDeleteEndpointTest {

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
    void deleteProjectExistingIdRemovesFromListAndGetByIdNow404() {
        Payloads.ProjectPayload newProject = TodoApiHelper.buildProject(
                "delete me", "to delete", true, false);
        Response createResponse = TodoApiHelper.createProject(newProject);
        String createdId = createResponse.body().as(Payloads.ProjectPayload.class).id;

        Response deleteResponse = TodoApiHelper.deleteProject(createdId);
        Assertions.assertEquals(200, deleteResponse.getStatusCode());

        Response getResponse = TodoApiHelper.getProjectById(createdId);
        Assertions.assertEquals(404, getResponse.getStatusCode());
    }

    @Test
    void deleteProjectTwiceSecondDeleteReturns404WithCouldNotFind() {
        Payloads.ProjectPayload newProject = TodoApiHelper.buildProject(
                "delete twice", "to delete", true, false);
        Response createResponse = TodoApiHelper.createProject(newProject);
        String createdId = createResponse.body().as(Payloads.ProjectPayload.class).id;

        Response deleteResponse = TodoApiHelper.deleteProject(createdId);
        Assertions.assertEquals(200, deleteResponse.getStatusCode());

        Response deleteAgain = TodoApiHelper.deleteProject(createdId);
        Assertions.assertEquals(404, deleteAgain.getStatusCode());

        Payloads.ErrorMessageResponse errors =
                deleteAgain.body().as(Payloads.ErrorMessageResponse.class);
        Assertions.assertFalse(errors.errorMessages.isEmpty());
    }

    @Test
    void deleteProjectNonexistentIdReturns404() {
        Response response = TodoApiHelper.deleteProject("99999");

        Assertions.assertEquals(404, response.getStatusCode());
    }
}
