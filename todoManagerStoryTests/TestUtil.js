const chai = require("chai");
const chaiHttp = require("chai-http");
chai.use(chaiHttp);

const HOST = "http://localhost:4567";
const TODOS_ENDPOINT = "/todos";
const PROJECTS_ENDPOINT = "/projects";

async function isServiceRunning() {
  try {
    const response = await chai.request(HOST).get(TODOS_ENDPOINT);
    return response.status === 200;
  } catch (e) {
    return false;
  }
}

async function getAllTodoIds() {
  const response = await chai.request(HOST).get(TODOS_ENDPOINT);
  if (response.body.todos) {
    return response.body.todos.map((todo) => todo.id);
  }
  return [];
}

async function getAllProjectIds() {
  const response = await chai.request(HOST).get(PROJECTS_ENDPOINT);
  if (response.body.projects) {
    return response.body.projects.map((project) => project.id);
  }
  return [];
}

async function getTodoIdByTitle(title) {
  const response = await chai
    .request(HOST)
    .get(TODOS_ENDPOINT)
    .query({ title });
  if (response.body.todos && response.body.todos.length > 0) {
    return response.body.todos[0].id;
  }
  return null;
}

async function getProjectIdByTitle(title) {
  const response = await chai
    .request(HOST)
    .get(PROJECTS_ENDPOINT)
    .query({ title });
  if (response.body.projects && response.body.projects.length > 0) {
    return response.body.projects[0].id;
  }
  return null;
}

async function deleteTodo(id) {
  await chai.request(HOST).delete(`${TODOS_ENDPOINT}/${id}`);
}

async function deleteProject(id) {
  await chai.request(HOST).delete(`${PROJECTS_ENDPOINT}/${id}`);
}

module.exports = {
  HOST,
  TODOS_ENDPOINT,
  PROJECTS_ENDPOINT,
  isServiceRunning,
  getAllTodoIds,
  getAllProjectIds,
  getTodoIdByTitle,
  getProjectIdByTitle,
  deleteTodo,
  deleteProject,
};
