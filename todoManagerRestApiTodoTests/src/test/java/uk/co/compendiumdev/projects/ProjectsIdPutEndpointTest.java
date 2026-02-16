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

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.sparkstart.Port;
import uk.co.compendiumdev.todos.helpers.Payloads;
import uk.co.compendiumdev.todos.helpers.TodoApiHelper;

/**
 * Tests for PUT /projects/:id (JSON).
 */
@TestMethodOrder(MethodOrderer.Random.class)
class ProjectsIdPutEndpointTest {

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
    void putProjectExistingIdReplacesAllFields() {
        Payloads.ProjectPayload newProject = TodoApiHelper.buildProject(
                "put before", "before", true, false);
        Response createResponse = TodoApiHelper.createProject(newProject);
        String createdId = createResponse.body().as(Payloads.ProjectPayload.class).id;

        Payloads.ProjectPayload update = TodoApiHelper.buildProject(
                "put after", "after", false, true);
        Response updateResponse = TodoApiHelper.updateProject(createdId, update);

        Assertions.assertEquals(200, updateResponse.getStatusCode());

        Response getResponse = TodoApiHelper.getProjectById(createdId);
        Payloads.ProjectPayload retrieved = getResponse.body()
                .as(Payloads.ProjectsPayload.class).projects.get(0);

        Assertions.assertEquals("put after", retrieved.title);
        Assertions.assertEquals("after", retrieved.description);
        Assertions.assertEquals(false, retrieved.active);
        Assertions.assertEquals(true, retrieved.completed);
    }

    @Test
    void putProjectNonexistentIdReturns404() {
        Payloads.ProjectPayload update = TodoApiHelper.buildProject(
                "missing", "missing", true, false);

        Response response = TodoApiHelper.updateProject("99999", update);

        Assertions.assertEquals(404, response.getStatusCode());
    }

    @Test
    void putProjectWrongTypeFieldReturns4xx() {
        Payloads.ProjectPayload newProject = TodoApiHelper.buildProject(
            "put wrong type", "before", true, false);
        Response createResponse = TodoApiHelper.createProject(newProject);
        String createdId = createResponse.body().as(Payloads.ProjectPayload.class).id;

        String rawJson = "{\"title\":\"bad put\",\"active\":\"notBoolean\"}";

        Response response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(rawJson)
            .put(Environment.getEnv("/projects/" + createdId));

        Assertions.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500);
    }
}
