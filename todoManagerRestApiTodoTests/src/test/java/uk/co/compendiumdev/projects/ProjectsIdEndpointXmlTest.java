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

import io.restassured.response.Response;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.sparkstart.Port;
import uk.co.compendiumdev.todos.helpers.Payloads;
import uk.co.compendiumdev.todos.helpers.TodoApiHelper;

/**
 * Tests for the /projects/:id endpoint (XML).
 */
@TestMethodOrder(MethodOrderer.Random.class)
class ProjectsIdEndpointXmlTest {

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
    void getProjectByIdAcceptXmlReturnsXml() {
        Payloads.ProjectPayload newProject = TodoApiHelper.buildProject(
                "xml get", "xml description", true, false);
        Response createResponse = TodoApiHelper.createProject(newProject);
        String createdId = createResponse.body().as(Payloads.ProjectPayload.class).id;

        Response response = TodoApiHelper.getProjectByIdAsXml(createdId);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(
                response.getContentType().contains("application/xml"));

        String title = response.xmlPath().getString("projects.project[0].title");
        Assertions.assertEquals("xml get", title);
    }
}
