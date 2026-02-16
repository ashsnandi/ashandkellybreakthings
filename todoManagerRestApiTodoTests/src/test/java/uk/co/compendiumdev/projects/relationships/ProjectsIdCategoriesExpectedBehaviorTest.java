package uk.co.compendiumdev.projects.relationships;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.restassured.response.Response;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.sparkstart.Port;
import uk.co.compendiumdev.todos.helpers.TodoApiHelper;

/**
 * Expected behavior tests for GET /projects/:id/categories.
 */
@TestMethodOrder(MethodOrderer.Random.class)
class ProjectsIdCategoriesExpectedBehaviorTest {

    @BeforeAll
    static void startServer() {
        Environment.getBaseUri();
        Assumptions.assumeTrue(
                Port.inUse("localhost", 4567),
                "Server is not running on localhost:4567");
    }

    @Test
    void getProjectCategoriesNonexistentProjectExpected404() {
        Response response = TodoApiHelper.getProjectCategories("99999");

        Assertions.assertEquals(404, response.getStatusCode());
    }
}
