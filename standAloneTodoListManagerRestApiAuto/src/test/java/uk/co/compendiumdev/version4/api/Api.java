package uk.co.compendiumdev.version4.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import uk.co.compendiumdev.sparkstart.Environment;

public class Api {

    /*
        API
     */

    public static Response createTodo(Payloads.TodoPayload todo){

        final Response response = RestAssured.
                given().
                contentType(ContentType.JSON).
                body(todo).
                post(Environment.getEnv("/todos")).
                andReturn();

        return response;
    }

    public static Response createProject(Payloads.ProjectPayload project){

        final Response response = RestAssured.
                given().
                contentType(ContentType.JSON).
                body(project).
                post(Environment.getEnv("/projects")).
                andReturn();

        return response;
    }

    public static Response updateProject(int projectId, Payloads.ProjectPayload project){

        final Response response = RestAssured.
                given().
                contentType(ContentType.JSON).
                body(project).
                put(Environment.getEnv("/projects/" + projectId)).
                andReturn();

        return response;
    }

    public static Response deleteProject(int projectId){

        final Response response = RestAssured.
                delete(Environment.getEnv("/projects/" + projectId)).
                andReturn();

        return response;
    }
}
