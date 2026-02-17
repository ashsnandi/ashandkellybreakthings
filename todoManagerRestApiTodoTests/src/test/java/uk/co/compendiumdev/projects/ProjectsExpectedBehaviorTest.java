package uk.co.compendiumdev.projects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
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
 * Expected behavior tests that may fail if implementation differs from documentation.
 */
@TestMethodOrder(MethodOrderer.Random.class)
@ExtendWith(uk.co.compendiumdev.todos.helpers.TestNameLogger.class)
class ProjectsExpectedBehaviorTest {

    @BeforeAll
    static void startServer() {
        Environment.getBaseUri();
        Assumptions.assumeTrue(
                Port.inUse("localhost", 4567),
                "Server is not running on localhost:4567");
    }

    @Test
    void createProjectMissingTitleExpectedToFailDocumentedExpectation() {
        Payloads.ProjectPayload newProject = new Payloads.ProjectPayload();
        newProject.description = "missing title should be rejected";

        Response response = TodoApiHelper.createProject(newProject);

        Assertions.assertEquals(400, response.getStatusCode());
    }
}
