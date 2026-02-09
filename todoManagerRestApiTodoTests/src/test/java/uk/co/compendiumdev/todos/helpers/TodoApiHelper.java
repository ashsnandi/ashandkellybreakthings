package uk.co.compendiumdev.todos.helpers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import uk.co.compendiumdev.sparkstart.Environment;

public class TodoApiHelper {

    // ---- JSON endpoints ----

    public static Response getAllTodos() {
        return RestAssured.get(Environment.getEnv("/todos"));
    }

    public static Response headTodos() {
        return RestAssured.head(Environment.getEnv("/todos"));
    }

    public static Response getTodoById(String id) {
        return RestAssured.get(Environment.getEnv("/todos/" + id));
    }

    public static Response createTodo(Payloads.TodoPayload todo) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(todo)
                .post(Environment.getEnv("/todos"));
    }

    public static Response amendTodo(String id, Payloads.TodoPayload todo) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(todo)
                .post(Environment.getEnv("/todos/" + id));
    }

    public static Response updateTodo(String id, Payloads.TodoPayload todo) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(todo)
                .put(Environment.getEnv("/todos/" + id));
    }

    public static Response deleteTodo(String id) {
        return RestAssured.delete(Environment.getEnv("/todos/" + id));
    }

    public static Response getTasksOfTodo(String todoId) {
        return RestAssured.get(Environment.getEnv("/todos/" + todoId + "/tasksof"));
    }

    public static Response getTodosByTitle(String title) {
        return RestAssured.given()
                .queryParam("title", title)
                .get(Environment.getEnv("/todos"));
    }

    public static Response createTodoWithRawJson(String rawJson) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(rawJson)
                .post(Environment.getEnv("/todos"));
    }

    // ---- XML endpoints ----

    public static Response getAllTodosAsXml() {
        return RestAssured.given()
                .accept(ContentType.XML)
                .get(Environment.getEnv("/todos"));
    }

    public static Response getTodoByIdAsXml(String id) {
        return RestAssured.given()
                .accept(ContentType.XML)
                .get(Environment.getEnv("/todos/" + id));
    }

    public static Response createTodoWithRawXml(String rawXml) {
        return RestAssured.given()
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .body(rawXml)
                .post(Environment.getEnv("/todos"));
    }

    public static Response amendTodoWithXml(String id, String xmlBody) {
        return RestAssured.given()
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .body(xmlBody)
                .post(Environment.getEnv("/todos/" + id));
    }

    public static Response updateTodoWithXml(String id, String xmlBody) {
        return RestAssured.given()
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .body(xmlBody)
                .put(Environment.getEnv("/todos/" + id));
    }

    public static Response getTasksOfTodoAsXml(String todoId) {
        return RestAssured.given()
                .accept(ContentType.XML)
                .get(Environment.getEnv("/todos/" + todoId + "/tasksof"));
    }

    public static Response getTodosByTitleAsXml(String title) {
        return RestAssured.given()
                .accept(ContentType.XML)
                .queryParam("title", title)
                .get(Environment.getEnv("/todos"));
    }

    // ---- Project helpers (for tasksof tests) ----

    public static Response createProject(Payloads.ProjectPayload project) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(project)
                .post(Environment.getEnv("/projects"));
    }

    public static Response linkTodoToProject(String projectId, String todoId) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"id\":\"" + todoId + "\"}")
                .post(Environment.getEnv("/projects/" + projectId + "/tasks"));
    }

    // ---- Factory ----

    public static Payloads.TodoPayload buildTodo(String title, String description, Boolean doneStatus) {
        Payloads.TodoPayload todo = new Payloads.TodoPayload();
        todo.title = title;
        todo.description = description;
        todo.doneStatus = doneStatus;
        return todo;
    }
}