package uk.co.compendiumdev.projects.relationships;

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
 * Tests for GET /projects/:id/categories (JSON + XML).
 */
@TestMethodOrder(MethodOrderer.Random.class)
@ExtendWith(uk.co.compendiumdev.todos.helpers.TestNameLogger.class)
class ProjectsIdCategoriesEndpointTest {

    private Map<String, Payloads.ProjectPayload> savedProjects;
    private Map<String, Payloads.CategoryPayload> savedCategories;

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
        Response projectResponse = TodoApiHelper.getAllProjects();
        Payloads.ProjectsPayload projects = projectResponse.body().as(Payloads.ProjectsPayload.class);
        if (projects.projects != null) {
            for (Payloads.ProjectPayload project : projects.projects) {
                savedProjects.put(project.id, project);
            }
        }

        savedCategories = new HashMap<>();
        Response categoryResponse = TodoApiHelper.getAllCategories();
        Payloads.CategoriesPayload categories =
                categoryResponse.body().as(Payloads.CategoriesPayload.class);
        if (categories.categories != null) {
            for (Payloads.CategoryPayload category : categories.categories) {
                savedCategories.put(category.id, category);
            }
        }
    }

    @AfterEach
    void restoreState() {
        Response projectResponse = TodoApiHelper.getAllProjects();
        Payloads.ProjectsPayload projects = projectResponse.body().as(Payloads.ProjectsPayload.class);
        if (projects.projects != null) {
            for (Payloads.ProjectPayload project : projects.projects) {
                if (!savedProjects.containsKey(project.id)) {
                    TodoApiHelper.deleteProject(project.id);
                }
            }
        }

        Response categoryResponse = TodoApiHelper.getAllCategories();
        Payloads.CategoriesPayload categories =
                categoryResponse.body().as(Payloads.CategoriesPayload.class);
        if (categories.categories != null) {
            for (Payloads.CategoryPayload category : categories.categories) {
                if (!savedCategories.containsKey(category.id)) {
                    TodoApiHelper.deleteCategory(category.id);
                }
            }
        }
    }

    @Test
    void getProjectCategoriesExistingProjectReturns200() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "categories project", "categories", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        Response response = TodoApiHelper.getProjectCategories(projectId);

        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    void getProjectCategoriesAcceptXmlReturnsXml() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "categories xml", "categories", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        Response response = TodoApiHelper.getProjectCategoriesAsXml(projectId);

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.getContentType().contains("application/xml"));
    }
}
