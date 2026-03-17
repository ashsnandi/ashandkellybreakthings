const { Given, When, Then } = require("@cucumber/cucumber");
const chai = require("chai");
const { expect } = chai;
const chaiHttp = require("chai-http");
const utils = require("../TestUtil.js");

chai.use(chaiHttp);

const host = utils.HOST;
const todosEndpoint = utils.TODOS_ENDPOINT;
const projectsEndpoint = utils.PROJECTS_ENDPOINT;

function parseBoolean(value) {
  return value === "true";
}

async function getProjectById(projectId) {
  const response = await chai
    .request(host)
    .get(`${projectsEndpoint}/${projectId}`);
  expect(response).to.have.status(200);
  return response.body.projects[0];
}

async function getProjectByTitle(title) {
  const projectId = await utils.getProjectIdByTitle(title);
  expect(projectId, `Project \"${title}\" should exist`).to.not.equal(null);
  return getProjectById(projectId);
}

Given("a project with title {string}, description {string}, active {word}, and completed {word} exists", async function (title, description, active, completed) {
  const response = await chai
    .request(host)
    .post(projectsEndpoint)
    .send({
      title,
      description,
      active: parseBoolean(active),
      completed: parseBoolean(completed),
    });

  expect([200, 201]).to.include(response.status);
  this.currentProjectId = response.body.id;
});

When("a student creates a project with title {string}, description {string}, active {word}, and completed {word}", async function (title, description, active, completed) {
  this.response = await chai
    .request(host)
    .post(projectsEndpoint)
    .send({
      title,
      description,
      active: parseBoolean(active),
      completed: parseBoolean(completed),
    });

  if ([200, 201].includes(this.response.status)) {
    this.currentProjectId = this.response.body.id;
  }
});

When("a student creates a project with title {string} only", async function (title) {
  this.response = await chai
    .request(host)
    .post(projectsEndpoint)
    .send({ title });

  if ([200, 201].includes(this.response.status)) {
    this.currentProjectId = this.response.body.id;
  }
});

When("a student creates a project with title {string} and invalid active value {string}", async function (title, activeValue) {
  this.response = await chai
    .request(host)
    .post(projectsEndpoint)
    .send({ title, active: activeValue });
});

When("a student replaces the project with title {string} with title {string}, description {string}, active {word}, and completed {word}", async function (originalTitle, newTitle, newDescription, active, completed) {
  const projectId = await utils.getProjectIdByTitle(originalTitle);
  this.currentProjectId = projectId;
  this.response = await chai
    .request(host)
    .put(`${projectsEndpoint}/${projectId}`)
    .send({
      title: newTitle,
      description: newDescription,
      active: parseBoolean(active),
      completed: parseBoolean(completed),
    });
});

When("a student marks the project with title {string} as completed", async function (title) {
  const projectId = await utils.getProjectIdByTitle(title);
  this.currentProjectId = projectId;
  this.response = await chai
    .request(host)
    .post(`${projectsEndpoint}/${projectId}`)
    .send({ completed: true });
});

When("a student updates a project with nonexistent id {string} with title {string}", async function (id, title) {
  this.response = await chai
    .request(host)
    .put(`${projectsEndpoint}/${id}`)
    .send({ title });
});

When("a student deletes the project with title {string}", async function (title) {
  const projectId = await utils.getProjectIdByTitle(title);
  this.response = await chai
    .request(host)
    .delete(`${projectsEndpoint}/${projectId}`);
});

When("a student deletes a project with nonexistent id {string}", async function (id) {
  this.response = await chai
    .request(host)
    .delete(`${projectsEndpoint}/${id}`);
});

When("a student creates a category with title {string} and description {string} under the project with title {string}", async function (categoryTitle, description, projectTitle) {
  const projectId = await utils.getProjectIdByTitle(projectTitle);
  this.response = await chai
    .request(host)
    .post(`${projectsEndpoint}/${projectId}/categories`)
    .send({ title: categoryTitle, description });

  if ([200, 201].includes(this.response.status)) {
    this.currentCategoryId = this.response.body.id;
  }
});

When("a student creates a category with title {string} and description {string} under a project with id {string}", async function (categoryTitle, description, projectId) {
  this.response = await chai
    .request(host)
    .post(`${projectsEndpoint}/${projectId}/categories`)
    .send({ title: categoryTitle, description });
});

When("a student removes the todo with title {string} from the project with title {string}", async function (todoTitle, projectTitle) {
  const todoId = await utils.getTodoIdByTitle(todoTitle);
  const projectId = await utils.getProjectIdByTitle(projectTitle);
  this.response = await chai
    .request(host)
    .delete(`${projectsEndpoint}/${projectId}/tasks/${todoId}`);
});

Then("the project creation succeeds", function () {
  expect([200, 201]).to.include(this.response.status);
});

Then("the category creation succeeds", function () {
  expect([200, 201]).to.include(this.response.status);
});

Then("the request fails with a client error status", function () {
  expect(this.response.status).to.be.within(400, 499);
});

Then("the created project has title {string}, description {string}, active {word}, and completed {word}", async function (title, description, active, completed) {
  const project = await getProjectById(this.currentProjectId);
  expect(project.title).to.equal(title);
  expect(project.description).to.equal(description);
  expect(project.active).to.equal(parseBoolean(active));
  expect(project.completed).to.equal(parseBoolean(completed));
});

Then("the created project keeps title {string} with default active {word} and completed {word}", async function (title, active, completed) {
  const project = await getProjectById(this.currentProjectId);
  expect(project.title).to.equal(title);
  expect(["", null]).to.include(project.description ?? null);
  expect(project.active).to.equal(parseBoolean(active));
  expect(project.completed).to.equal(parseBoolean(completed));
});

Then("the project with title {string} has title {string}, description {string}, active {word}, and completed {word}", async function (lookupTitle, expectedTitle, description, active, completed) {
  const project = await getProjectByTitle(lookupTitle);
  expect(project.title).to.equal(expectedTitle);
  expect(project.description).to.equal(description);
  expect(project.active).to.equal(parseBoolean(active));
  expect(project.completed).to.equal(parseBoolean(completed));
});

Then("the project deletion succeeds with status {int}", function (status) {
  expect(this.response).to.have.status(status);
});

Then("the project with title {string} no longer exists", async function (title) {
  const response = await chai
    .request(host)
    .get(projectsEndpoint)
    .query({ title });
  const projects = response.body.projects || [];
  expect(projects).to.have.lengthOf(0, `Project \"${title}\" should no longer exist`);
});

Then("the category with title {string} appears under the project with title {string}", async function (categoryTitle, projectTitle) {
  const projectId = await utils.getProjectIdByTitle(projectTitle);
  const response = await chai
    .request(host)
    .get(`${projectsEndpoint}/${projectId}/categories`);
  expect(response).to.have.status(200);
  const categories = response.body.categories || [];
  const found = categories.some((category) => category.title === categoryTitle);
  expect(found, `Category \"${categoryTitle}\" should appear under project \"${projectTitle}\"`).to.be.true;
});

Then("the todo with title {string} does not appear in the tasks of project with title {string}", async function (todoTitle, projectTitle) {
  const projectId = await utils.getProjectIdByTitle(projectTitle);
  const response = await chai
    .request(host)
    .get(`${projectsEndpoint}/${projectId}/tasks`);
  expect(response).to.have.status(200);
  const tasks = response.body.todos || [];
  const found = tasks.some((task) => task.title === todoTitle);
  expect(found, `Todo \"${todoTitle}\" should not appear in project \"${projectTitle}\" tasks`).to.be.false;
});

Then("the todo with title {string} still exists", async function (title) {
  const response = await chai
    .request(host)
    .get(todosEndpoint)
    .query({ title });
  const todos = response.body.todos || [];
  expect(todos).to.have.lengthOf.at.least(1, `Todo \"${title}\" should still exist`);
});
