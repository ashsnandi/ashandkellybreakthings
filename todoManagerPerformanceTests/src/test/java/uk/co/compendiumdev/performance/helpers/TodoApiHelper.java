package uk.co.compendiumdev.performance.helpers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import uk.co.compendiumdev.sparkstart.Environment;

public class TodoApiHelper {

    public static Response getAllTodos() {
        return RestAssured.get(Environment.getEnv("/todos"));
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

    public static Response updateTodo(String id, Payloads.TodoPayload todo) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(todo)
                .put(Environment.getEnv("/todos/" + id));
    }

    public static Response deleteTodo(String id) {
        return RestAssured.delete(Environment.getEnv("/todos/" + id));
    }

    public static Payloads.TodoPayload buildTodo(String title, String description, Boolean doneStatus) {
        Payloads.TodoPayload todo = new Payloads.TodoPayload();
        todo.title = title;
        todo.description = description;
        todo.doneStatus = doneStatus;
        return todo;
    }
}
