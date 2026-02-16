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
 * Tests for the /projects collection endpoint (XML).
 */
@TestMethodOrder(MethodOrderer.Random.class)
class ProjectsEndpointXmlTest {

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
    void getProjectsAcceptXmlReturns200XmlPayload() {
        Response response = TodoApiHelper.getAllProjectsAsXml();

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(
                response.getContentType().contains("application/xml"));
    }

    @Test
    void createProjectXmlBodyReturns200Or201AndListedInProjects() {
        String xmlBody = "<project>"
                + "<title>XML project</title>"
                + "<description>created via xml</description>"
                + "<active>true</active>"
                + "<completed>false</completed>"
                + "</project>";

        Response response = TodoApiHelper.createProjectWithRawXml(xmlBody);

        Assertions.assertTrue(
                response.getStatusCode() == 201 || response.getStatusCode() == 200);

        String id = response.xmlPath().getString("project.id");
        Assertions.assertNotNull(id);

        Response allProjects = TodoApiHelper.getAllProjects();
        Payloads.ProjectsPayload projects = allProjects.body().as(Payloads.ProjectsPayload.class);
        Assertions.assertTrue(
                projects.projects.stream().anyMatch(project -> id.equals(project.id)));
    }

    @Test
    void createProjectMalformedXmlReturns4xx() {
        String malformedXml = "<project><title>broken xml";

        Response response = TodoApiHelper.createProjectWithRawXml(malformedXml);

        Assertions.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500);
    }
}
