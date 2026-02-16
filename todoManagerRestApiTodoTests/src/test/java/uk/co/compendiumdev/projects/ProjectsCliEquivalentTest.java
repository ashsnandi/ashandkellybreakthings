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
 * CLI-equivalent tests using raw JSON payloads and headers.
 */
@TestMethodOrder(MethodOrderer.Random.class)
class ProjectsCliEquivalentTest {

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
    void cliEquivalentCreateProjectSameAsSessionCurlReturnsSuccess() {
        String rawJson = "{\"title\":\"cli project\",\"description\":\"cli desc\"," 
                + "\"active\":true,\"completed\":false}";

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(rawJson)
                .post(Environment.getEnv("/projects"));

        Assertions.assertTrue(
                response.getStatusCode() == 201 || response.getStatusCode() == 200);
    }

    @Test
    void cliEquivalentPutProjectSameAsSessionCurlReturnsSuccess() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "cli put", "before", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        String rawJson = "{\"title\":\"cli put updated\",\"description\":\"updated\"," 
                + "\"active\":false,\"completed\":true}";

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(rawJson)
                .put(Environment.getEnv("/projects/" + projectId));

        Assertions.assertTrue(
                response.getStatusCode() == 201 || response.getStatusCode() == 200);
    }

    @Test
    void cliEquivalentBadJsonSameAsSessionCurlReturnsMalformedJsonError() {
        String rawJson = "{title: broken json, missing quotes";

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(rawJson)
                .post(Environment.getEnv("/projects"));

        Assertions.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500);

        Payloads.ErrorMessageResponse errors =
                response.body().as(Payloads.ErrorMessageResponse.class);
        Assertions.assertFalse(errors.errorMessages.isEmpty());
    }
}
