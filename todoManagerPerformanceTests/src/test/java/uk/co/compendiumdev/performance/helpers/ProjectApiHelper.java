package uk.co.compendiumdev.performance.helpers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import uk.co.compendiumdev.sparkstart.Environment;

public class ProjectApiHelper {

    public static Response createProject(Payloads.ProjectPayload project) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(project)
                .post(Environment.getEnv("/projects"));
    }

    public static Response updateProject(String id, Payloads.ProjectPayload project) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(project)
                .put(Environment.getEnv("/projects/" + id));
    }

    public static Response deleteProject(String id) {
        return RestAssured.delete(Environment.getEnv("/projects/" + id));
    }

    public static Payloads.ProjectPayload buildProject(String title, String description, boolean completed, boolean active) {
        Payloads.ProjectPayload project = new Payloads.ProjectPayload();
        project.title = title;
        project.description = description;
        project.completed = completed;
        project.active = active;
        return project;
    }
}
