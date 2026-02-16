package uk.co.compendiumdev.projects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.restassured.response.Response;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.sparkstart.Port;
import uk.co.compendiumdev.todos.helpers.Payloads;
import uk.co.compendiumdev.todos.helpers.TodoApiHelper;

@TestMethodOrder(MethodOrderer.Random.class)
class ProjectsRandomOrderSmokeTest {

    @BeforeAll
    static void startServer() {
        Environment.getBaseUri();
        Assumptions.assumeTrue(
                Port.inUse("localhost", 4567),
                "Server is not running on localhost:4567");
    }

    @Test
    void smokeGetProjectsReturns200() {
        Response response = TodoApiHelper.getAllProjects();

        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    void smokeGetProjectByIdReturns200() {
        Response response = TodoApiHelper.getProjectById("1");

        Assertions.assertEquals(200, response.getStatusCode());

        Payloads.ProjectsPayload projects = response.body().as(Payloads.ProjectsPayload.class);
        Assertions.assertNotNull(projects.projects);
        Assertions.assertFalse(projects.projects.isEmpty());
    }
}
