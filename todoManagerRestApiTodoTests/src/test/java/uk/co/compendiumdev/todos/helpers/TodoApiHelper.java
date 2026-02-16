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

    public static Response getAllProjects() {
        return RestAssured.get(Environment.getEnv("/projects"));
    }

    public static Response headTodos() {
        return RestAssured.head(Environment.getEnv("/todos"));
    }

    public static Response getTodoById(String id) {
        return RestAssured.get(Environment.getEnv("/todos/" + id));
    }

    public static Response getProjectById(String id) {
        return RestAssured.get(Environment.getEnv("/projects/" + id));
    }

    public static Response createTodo(Payloads.TodoPayload todo) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(todo)
                .post(Environment.getEnv("/todos"));
    }

    public static Response createProject(Payloads.ProjectPayload project) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(project)
                .post(Environment.getEnv("/projects"));
    }

    public static Response amendTodo(String id, Payloads.TodoPayload todo) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(todo)
                .post(Environment.getEnv("/todos/" + id));
    }

    public static Response amendProject(String id, Payloads.ProjectPayload project) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(project)
                .post(Environment.getEnv("/projects/" + id));
    }

    public static Response updateTodo(String id, Payloads.TodoPayload todo) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(todo)
                .put(Environment.getEnv("/todos/" + id));
    }

    public static Response updateProject(String id, Payloads.ProjectPayload project) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(project)
                .put(Environment.getEnv("/projects/" + id));
    }

    public static Response deleteTodo(String id) {
        return RestAssured.delete(Environment.getEnv("/todos/" + id));
    }

    public static Response deleteProject(String id) {
        return RestAssured.delete(Environment.getEnv("/projects/" + id));
    }

    public static Response getProjectTasks(String projectId) {
        return RestAssured.get(Environment.getEnv("/projects/" + projectId + "/tasks"));
    }

    public static Response getProjectCategories(String projectId) {
        return RestAssured.get(Environment.getEnv("/projects/" + projectId + "/categories"));
    }

    public static Response createTaskUnderProject(String projectId, Payloads.TodoPayload todo) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(todo)
                .post(Environment.getEnv("/projects/" + projectId + "/tasks"));
    }

    public static Response createCategoryUnderProject(
            String projectId,
            Payloads.CategoryPayload category) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(category)
                .post(Environment.getEnv("/projects/" + projectId + "/categories"));
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

    public static Response createProjectWithRawJson(String rawJson) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(rawJson)
                .post(Environment.getEnv("/projects"));
    }

    public static Response createTaskUnderProjectWithRawJson(String projectId, String rawJson) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(rawJson)
                .post(Environment.getEnv("/projects/" + projectId + "/tasks"));
    }

    public static Response createCategoryUnderProjectWithRawJson(
            String projectId,
            String rawJson) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(rawJson)
                .post(Environment.getEnv("/projects/" + projectId + "/categories"));
    }

    // ---- XML endpoints ----

    public static Response getAllTodosAsXml() {
        return RestAssured.given()
                .accept(ContentType.XML)
                .get(Environment.getEnv("/todos"));
    }

    public static Response getAllProjectsAsXml() {
        return RestAssured.given()
                .accept(ContentType.XML)
                .get(Environment.getEnv("/projects"));
    }

    public static Response getTodoByIdAsXml(String id) {
        return RestAssured.given()
                .accept(ContentType.XML)
                .get(Environment.getEnv("/todos/" + id));
    }

    public static Response getProjectByIdAsXml(String id) {
        return RestAssured.given()
                .accept(ContentType.XML)
                .get(Environment.getEnv("/projects/" + id));
    }

    public static Response createTodoWithRawXml(String rawXml) {
        return RestAssured.given()
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .body(rawXml)
                .post(Environment.getEnv("/todos"));
    }

    public static Response createProjectWithRawXml(String rawXml) {
        return RestAssured.given()
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .body(rawXml)
                .post(Environment.getEnv("/projects"));
    }

    public static Response amendTodoWithXml(String id, String xmlBody) {
        return RestAssured.given()
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .body(xmlBody)
                .post(Environment.getEnv("/todos/" + id));
    }

    public static Response amendProjectWithXml(String id, String xmlBody) {
        return RestAssured.given()
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .body(xmlBody)
                .post(Environment.getEnv("/projects/" + id));
    }

    public static Response updateTodoWithXml(String id, String xmlBody) {
        return RestAssured.given()
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .body(xmlBody)
                .put(Environment.getEnv("/todos/" + id));
    }

    public static Response updateProjectWithXml(String id, String xmlBody) {
        return RestAssured.given()
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .body(xmlBody)
                .put(Environment.getEnv("/projects/" + id));
    }

    public static Response getTasksOfTodoAsXml(String todoId) {
        return RestAssured.given()
                .accept(ContentType.XML)
                .get(Environment.getEnv("/todos/" + todoId + "/tasksof"));
    }

    public static Response getProjectTasksAsXml(String projectId) {
        return RestAssured.given()
                .accept(ContentType.XML)
                .get(Environment.getEnv("/projects/" + projectId + "/tasks"));
    }

    public static Response getProjectCategoriesAsXml(String projectId) {
        return RestAssured.given()
                .accept(ContentType.XML)
                .get(Environment.getEnv("/projects/" + projectId + "/categories"));
    }

    public static Response getTodosByTitleAsXml(String title) {
        return RestAssured.given()
                .accept(ContentType.XML)
                .queryParam("title", title)
                .get(Environment.getEnv("/todos"));
    }

    public static Response createTaskUnderProjectWithRawXml(String projectId, String rawXml) {
        return RestAssured.given()
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .body(rawXml)
                .post(Environment.getEnv("/projects/" + projectId + "/tasks"));
    }

    public static Response createCategoryUnderProjectWithRawXml(
            String projectId,
            String rawXml) {
        return RestAssured.given()
                .contentType(ContentType.XML)
                .accept(ContentType.XML)
                .body(rawXml)
                .post(Environment.getEnv("/projects/" + projectId + "/categories"));
    }

    // ---- Project helpers (for tasksof tests) ----

    public static Response linkTodoToProject(String projectId, String todoId) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"id\":\"" + todoId + "\"}")
                .post(Environment.getEnv("/projects/" + projectId + "/tasks"));
    }

    public static Response getAllCategories() {
        return RestAssured.get(Environment.getEnv("/categories"));
    }

    public static Response deleteCategory(String id) {
        return RestAssured.delete(Environment.getEnv("/categories/" + id));
    }

    // ---- Factory ----

    public static Payloads.TodoPayload buildTodo(String title, String description, Boolean doneStatus) {
        Payloads.TodoPayload todo = new Payloads.TodoPayload();
        todo.title = title;
        todo.description = description;
        todo.doneStatus = doneStatus;
        return todo;
    }

    public static Payloads.ProjectPayload buildProject(
            String title,
            String description,
            Boolean active,
            Boolean completed) {
        Payloads.ProjectPayload project = new Payloads.ProjectPayload();
        project.title = title;
        project.description = description;
        project.active = active;
        project.completed = completed;
        return project;
    }

    public static Payloads.CategoryPayload buildCategory(String title, String description) {
        Payloads.CategoryPayload category = new Payloads.CategoryPayload();
        category.title = title;
        category.description = description;
        return category;
    }
}