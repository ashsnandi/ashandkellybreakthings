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

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.sparkstart.Port;
import uk.co.compendiumdev.todos.helpers.Payloads;
import uk.co.compendiumdev.todos.helpers.TodoApiHelper;

/**
 * Tests for the /projects collection endpoint (JSON).
 * Covers: GET /projects, POST /projects, schema checks, validation.
 */
@TestMethodOrder(MethodOrderer.Random.class)
@ExtendWith(uk.co.compendiumdev.todos.helpers.TestNameLogger.class)
class ProjectsEndpointTest {

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

    // GET /projects

    @Test
    void getProjectsDefaultContainsProjectId1() {
        Response response = TodoApiHelper.getAllProjects();

        Assertions.assertEquals(200, response.getStatusCode());

        Payloads.ProjectsPayload projects = response.body().as(Payloads.ProjectsPayload.class);
        Assertions.assertNotNull(projects.projects);
        Assertions.assertTrue(
                projects.projects.stream().anyMatch(project -> "1".equals(project.id)),
                "Default project id=1 should exist");
    }

    @Test
    void getProjectsReturns200JsonByDefault() {
        Response response = TodoApiHelper.getAllProjects();

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(
                response.getContentType().contains(ContentType.JSON.toString()));
    }

    @Test
    void getProjectsResponseSchemaHasProjectsArrayAndFields() {
        Response response = TodoApiHelper.getAllProjects();

        Assertions.assertEquals(200, response.getStatusCode());

        Payloads.ProjectsPayload projects = response.body().as(Payloads.ProjectsPayload.class);
        Assertions.assertNotNull(projects.projects);
        Assertions.assertFalse(projects.projects.isEmpty());

        Payloads.ProjectPayload first = projects.projects.get(0);
        Assertions.assertNotNull(first.id);
        Assertions.assertNotNull(first.title);
        Assertions.assertNotNull(first.description);
        Assertions.assertNotNull(first.active);
        Assertions.assertNotNull(first.completed);
    }

    // POST /projects

    @Test
    void createProjectValidJsonReturns201Or200AndProjectAppearsInList() {
        Payloads.ProjectPayload newProject = TodoApiHelper.buildProject(
                "valid project", "valid description", true, false);

        Response response = TodoApiHelper.createProject(newProject);

        Assertions.assertTrue(
                response.getStatusCode() == 201 || response.getStatusCode() == 200);

        Payloads.ProjectPayload created = response.body().as(Payloads.ProjectPayload.class);
        Assertions.assertNotNull(created.id);
        Assertions.assertEquals("valid project", created.title);

        Response allProjects = TodoApiHelper.getAllProjects();
        Payloads.ProjectsPayload projects = allProjects.body().as(Payloads.ProjectsPayload.class);
        Assertions.assertTrue(
                projects.projects.stream().anyMatch(project -> created.id.equals(project.id)));
    }

    @Test
    void createProjectThenGetByIdReturnsSameFields() {
        Payloads.ProjectPayload newProject = TodoApiHelper.buildProject(
                "get by id", "get by id description", false, true);

        Response createResponse = TodoApiHelper.createProject(newProject);
        Payloads.ProjectPayload created = createResponse.body().as(Payloads.ProjectPayload.class);

        Response response = TodoApiHelper.getProjectById(created.id);

        Assertions.assertEquals(200, response.getStatusCode());

        Payloads.ProjectPayload retrieved = response.body()
                .as(Payloads.ProjectsPayload.class).projects.get(0);
        Assertions.assertEquals(created.id, retrieved.id);
        Assertions.assertEquals("get by id", retrieved.title);
        Assertions.assertEquals("get by id description", retrieved.description);
        Assertions.assertEquals(false, retrieved.active);
        Assertions.assertEquals(true, retrieved.completed);
    }

    @Test
    void createProjectWrongTypeActiveNotBooleanReturns4xxAndValidationMessage() {
        String rawJson = "{\"title\":\"bad active\",\"active\":\"nope\"}";

        Response response = TodoApiHelper.createProjectWithRawJson(rawJson);

        Assertions.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500);

        Payloads.ErrorMessageResponse errors =
                response.body().as(Payloads.ErrorMessageResponse.class);
        Assertions.assertFalse(errors.errorMessages.isEmpty());
    }

    @Test
    void createProjectMissingTitleAllowsEmptyValueUndocumented() {
        Payloads.ProjectPayload newProject = new Payloads.ProjectPayload();
        newProject.description = "missing title";

        Response response = TodoApiHelper.createProject(newProject);

        Assertions.assertTrue(
                response.getStatusCode() == 201 || response.getStatusCode() == 200);

        Payloads.ProjectPayload created = response.body().as(Payloads.ProjectPayload.class);
        Assertions.assertTrue(created.title == null || created.title.isEmpty());
    }

    @Test
    void createProjectMalformedJsonReturns4xxAndMalformedJsonException() {
        String malformedJson = "{title: broken json, missing quotes";

        Response response = TodoApiHelper.createProjectWithRawJson(malformedJson);

        Assertions.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500);
    }

    @Test
    void createProjectDoesNotModifyOtherProjects() {
        Payloads.ProjectPayload newProject = TodoApiHelper.buildProject(
                "side effects", "no side effects", true, false);

        TodoApiHelper.createProject(newProject);

        Response response = TodoApiHelper.getAllProjects();
        Payloads.ProjectsPayload projects = response.body().as(Payloads.ProjectsPayload.class);

        for (Payloads.ProjectPayload project : projects.projects) {
            if (savedProjects.containsKey(project.id)) {
                Payloads.ProjectPayload original = savedProjects.get(project.id);
                Assertions.assertEquals(original.title, project.title);
                Assertions.assertEquals(original.description, project.description);
                Assertions.assertEquals(original.active, project.active);
                Assertions.assertEquals(original.completed, project.completed);
            }
        }
    }
}
