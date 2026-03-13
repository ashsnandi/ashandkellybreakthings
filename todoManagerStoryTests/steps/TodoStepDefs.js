const { Given, When, Then } = require("@cucumber/cucumber");
const chai = require("chai");
const { expect } = chai;
const chaiHttp = require("chai-http");
const utils = require("../TestUtil.js");

chai.use(chaiHttp);

const host = utils.HOST;
const todosEndpoint = utils.TODOS_ENDPOINT;
const projectsEndpoint = utils.PROJECTS_ENDPOINT;

// ─── GIVEN ────────────────────────────────────────────────────────────────────

Given("a todo with title {string} and doneStatus {word} exists", async function (title, doneStatus) {
  const isDone = doneStatus === "true";
  const response = await chai
    .request(host)
    .post(todosEndpoint)
    .send({ title, doneStatus: isDone });
  expect(response).to.have.status(201);
  this.currentTodoId = response.body.id;
});

Given("a project with title {string} exists", async function (title) {
  const response = await chai
    .request(host)
    .post(projectsEndpoint)
    .send({ title });
  expect(response).to.have.status(201);
  this.currentProjectId = response.body.id;
});

Given("the todo with title {string} is linked to the project with title {string}", async function (todoTitle, projectTitle) {
  const todoId = await utils.getTodoIdByTitle(todoTitle);
  const projectId = await utils.getProjectIdByTitle(projectTitle);
  const response = await chai
    .request(host)
    .post(`${projectsEndpoint}/${projectId}/tasks`)
    .send({ id: todoId });
  expect(response).to.have.status(201);
});

// ─── WHEN ─────────────────────────────────────────────────────────────────────

When("a student creates a todo with title {string} and description {string}", async function (title, description) {
  this.response = await chai
    .request(host)
    .post(todosEndpoint)
    .send({ title, description });
  if (this.response.status === 201) {
    this.currentTodoId = this.response.body.id;
  }
});

When("a student creates a todo with title {string} and no description", async function (title) {
  this.response = await chai
    .request(host)
    .post(todosEndpoint)
    .send({ title });
  if (this.response.status === 201) {
    this.currentTodoId = this.response.body.id;
  }
});

When("a student marks the todo with title {string} as done", async function (title) {
  const todoId = await utils.getTodoIdByTitle(title);
  this.response = await chai
    .request(host)
    .post(`${todosEndpoint}/${todoId}`)
    .send({ doneStatus: true });
});

When("a student marks the todo with title {string} as not done", async function (title) {
  const todoId = await utils.getTodoIdByTitle(title);
  this.response = await chai
    .request(host)
    .post(`${todosEndpoint}/${todoId}`)
    .send({ doneStatus: false });
});

When("a student marks the todo with id {string} as done", async function (id) {
  this.response = await chai
    .request(host)
    .post(`${todosEndpoint}/${id}`)
    .send({ doneStatus: true });
});

When("a student assigns the todo with title {string} to the project with title {string}", async function (todoTitle, projectTitle) {
  const todoId = await utils.getTodoIdByTitle(todoTitle);
  const projectId = await utils.getProjectIdByTitle(projectTitle);
  this.response = await chai
    .request(host)
    .post(`${projectsEndpoint}/${projectId}/tasks`)
    .send({ id: todoId });
});

When("a student assigns the todo with title {string} to a project with id {string}", async function (todoTitle, projectId) {
  const todoId = await utils.getTodoIdByTitle(todoTitle);
  this.response = await chai
    .request(host)
    .post(`${projectsEndpoint}/${projectId}/tasks`)
    .send({ id: todoId });
});

When("a student deletes the todo with title {string}", async function (title) {
  const todoId = await utils.getTodoIdByTitle(title);
  this.response = await chai
    .request(host)
    .delete(`${todosEndpoint}/${todoId}`);
});

When("a student deletes a todo with nonexistent id {string}", async function (id) {
  this.response = await chai
    .request(host)
    .delete(`${todosEndpoint}/${id}`);
});

When("a student replaces the todo with title {string} with title {string} and description {string}", async function (originalTitle, newTitle, newDescription) {
  const todoId = await utils.getTodoIdByTitle(originalTitle);
  this.currentTodoId = todoId;
  this.response = await chai
    .request(host)
    .put(`${todosEndpoint}/${todoId}`)
    .send({ title: newTitle, description: newDescription });
});

When("a student amends the description of the todo with title {string} to {string}", async function (title, newDescription) {
  const todoId = await utils.getTodoIdByTitle(title);
  this.currentTodoId = todoId;
  this.response = await chai
    .request(host)
    .post(`${todosEndpoint}/${todoId}`)
    .send({ description: newDescription });
});

When("a student updates a todo with nonexistent id {string} with title {string}", async function (id, title) {
  this.response = await chai
    .request(host)
    .put(`${todosEndpoint}/${id}`)
    .send({ title });
});

// ─── THEN ─────────────────────────────────────────────────────────────────────

Then("the todo is created with status {int}", function (status) {
  expect(this.response).to.have.status(status);
});

Then("the created todo has title {string} and description {string}", async function (title, description) {
  const response = await chai
    .request(host)
    .get(`${todosEndpoint}/${this.currentTodoId}`);
  expect(response).to.have.status(200);
  const todo = response.body.todos[0];
  expect(todo.title).to.equal(title);
  expect(todo.description).to.equal(description);
});

Then("the created todo has an empty description", async function () {
  const response = await chai
    .request(host)
    .get(`${todosEndpoint}/${this.currentTodoId}`);
  expect(response).to.have.status(200);
  const todo = response.body.todos[0];
  expect(todo.description).to.equal("");
});

Then("the request fails with status {int}", function (status) {
  expect(this.response).to.have.status(status);
});

Then("the response contains an error message", function () {
  expect(this.response.body.errorMessages).to.be.an("array").that.is.not.empty;
});

Then("the todo with title {string} has doneStatus {string}", async function (title, expectedStatus) {
  const todoId = await utils.getTodoIdByTitle(title);
  const response = await chai
    .request(host)
    .get(`${todosEndpoint}/${todoId}`);
  expect(response).to.have.status(200);
  const todo = response.body.todos[0];
  expect(todo.doneStatus).to.equal(expectedStatus);
});

Then("the assignment succeeds with status {int}", function (status) {
  expect(this.response).to.have.status(status);
});

Then("the todo with title {string} appears in the tasks of project with title {string}", async function (todoTitle, projectTitle) {
  const projectId = await utils.getProjectIdByTitle(projectTitle);
  const response = await chai
    .request(host)
    .get(`${projectsEndpoint}/${projectId}/tasks`);
  expect(response).to.have.status(200);
  const tasks = response.body.todos || [];
  const found = tasks.some((task) => task.title === todoTitle);
  expect(found, `Todo "${todoTitle}" should appear in project "${projectTitle}" tasks`).to.be.true;
});

Then("the deletion succeeds with status {int}", function (status) {
  expect(this.response).to.have.status(status);
});

Then("the todo with title {string} no longer exists", async function (title) {
  const response = await chai
    .request(host)
    .get(todosEndpoint)
    .query({ title });
  const todos = response.body.todos || [];
  expect(todos).to.have.lengthOf(0, `Todo "${title}" should no longer exist`);
});

Then("the project with title {string} still exists", async function (title) {
  const response = await chai
    .request(host)
    .get(projectsEndpoint)
    .query({ title });
  expect(response.body.projects).to.be.an("array").with.lengthOf.at.least(1);
});

Then("the response has status {int}", function (status) {
  expect(this.response).to.have.status(status);
});

Then("the todo now has title {string} and description {string}", async function (title, description) {
  const response = await chai
    .request(host)
    .get(`${todosEndpoint}/${this.currentTodoId}`);
  expect(response).to.have.status(200);
  const todo = response.body.todos[0];
  expect(todo.title).to.equal(title);
  expect(todo.description).to.equal(description);
});

Then("the todo with title {string} has description {string}", async function (title, description) {
  const todoId = await utils.getTodoIdByTitle(title);
  const response = await chai
    .request(host)
    .get(`${todosEndpoint}/${todoId}`);
  expect(response).to.have.status(200);
  const todo = response.body.todos[0];
  expect(todo.description).to.equal(description);
});
