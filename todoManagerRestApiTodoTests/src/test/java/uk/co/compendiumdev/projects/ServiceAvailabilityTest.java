package uk.co.compendiumdev.projects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.restassured.response.Response;
import uk.co.compendiumdev.sparkstart.Environment;
import uk.co.compendiumdev.todos.helpers.TodoApiHelper;

class ServiceAvailabilityTest {

    @Test
    void serviceNotRunningShouldFailFast() {
        Environment.getBaseUri();
        Response response = TodoApiHelper.getAllProjects();

        Assertions.assertEquals(
            200,
            response.getStatusCode(),
            "GET /projects should succeed when the service is running");
    }
}
