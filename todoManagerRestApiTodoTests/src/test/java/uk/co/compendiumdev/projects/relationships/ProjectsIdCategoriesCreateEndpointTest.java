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
 * Tests for POST /projects/:id/categories (JSON + XML).
 */
@TestMethodOrder(MethodOrderer.Random.class)
@ExtendWith(uk.co.compendiumdev.todos.helpers.TestNameLogger.class)
class ProjectsIdCategoriesCreateEndpointTest {

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
    void createCategoryUnderProjectValidReturns201Or200AndCategoryListedUnderProject() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "category create", "create category", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        Payloads.CategoryPayload categoryPayload = TodoApiHelper.buildCategory(
                "category title", "category description");
        Response response = TodoApiHelper.createCategoryUnderProject(projectId, categoryPayload);

        Assertions.assertTrue(
                response.getStatusCode() == 201 || response.getStatusCode() == 200);

        Response listResponse = TodoApiHelper.getProjectCategories(projectId);
        Payloads.CategoriesPayload categories =
                listResponse.body().as(Payloads.CategoriesPayload.class);
        Assertions.assertTrue(
                categories.categories.stream().anyMatch(
                        category -> "category title".equals(category.title)));
    }

    @Test
    void createCategoryUnderProjectWithIdProvidedReturns4xxNotAllowedToSetId() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "category id", "with id", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        String rawJson = "{\"id\":\"123\",\"title\":\"category with id\"}";
        Response response = TodoApiHelper.createCategoryUnderProjectWithRawJson(projectId, rawJson);

        Assertions.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500);
    }

    @Test
    void createCategoryUnderProjectNonexistentProjectReturns404() {
        Payloads.CategoryPayload categoryPayload = TodoApiHelper.buildCategory(
                "missing parent", "missing parent");

        Response response = TodoApiHelper.createCategoryUnderProject("99999", categoryPayload);

        Assertions.assertEquals(404, response.getStatusCode());
    }

    @Test
    void createCategoryUnderProjectMalformedJsonReturns4xx() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "category malformed", "malformed", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        String rawJson = "{title: broken json, missing quotes";
        Response response = TodoApiHelper.createCategoryUnderProjectWithRawJson(projectId, rawJson);

        Assertions.assertTrue(response.getStatusCode() >= 400 && response.getStatusCode() < 500);
    }

    @Test
    void createCategoryUnderProjectAcceptXmlOrXmlSupportCheck() {
        Payloads.ProjectPayload projectPayload = TodoApiHelper.buildProject(
                "category xml", "xml", true, false);
        Response createProject = TodoApiHelper.createProject(projectPayload);
        String projectId = createProject.body().as(Payloads.ProjectPayload.class).id;

        String xmlBody = "<category>"
                + "<title>xml category title</title>"
                + "<description>xml category</description>"
                + "</category>";

        Response response = TodoApiHelper.createCategoryUnderProjectWithRawXml(projectId, xmlBody);

        Assertions.assertTrue(
                response.getStatusCode() == 201 || response.getStatusCode() == 200);
        Assertions.assertTrue(
                response.getContentType().contains("application/xml"));
    }
}
