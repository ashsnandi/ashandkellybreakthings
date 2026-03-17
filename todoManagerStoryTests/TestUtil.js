const chai = require("chai");
const chaiHttp = require("chai-http");
chai.use(chaiHttp);

const HOST = "http://localhost:4567";
const TODOS_ENDPOINT = "/todos";
const PROJECTS_ENDPOINT = "/projects";
const CATEGORIES_ENDPOINT = "/categories";

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

async function getAllCategoryIds() {
  const response = await chai.request(HOST).get(CATEGORIES_ENDPOINT);
  if (response.body.categories) {
    return response.body.categories.map((category) => category.id);
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

async function getCategoryIdByTitle(title) {
  const response = await chai
    .request(HOST)
    .get(CATEGORIES_ENDPOINT)
    .query({ title });
  if (response.body.categories && response.body.categories.length > 0) {
    return response.body.categories[0].id;
  }
  return null;
}

async function deleteTodo(id) {
  await chai.request(HOST).delete(`${TODOS_ENDPOINT}/${id}`);
}

async function deleteProject(id) {
  await chai.request(HOST).delete(`${PROJECTS_ENDPOINT}/${id}`);
}

async function deleteCategory(id) {
  await chai.request(HOST).delete(`${CATEGORIES_ENDPOINT}/${id}`);
}

module.exports = {
  HOST,
  TODOS_ENDPOINT,
  PROJECTS_ENDPOINT,
  CATEGORIES_ENDPOINT,
  isServiceRunning,
  getAllTodoIds,
  getAllProjectIds,
  getAllCategoryIds,
  getTodoIdByTitle,
  getProjectIdByTitle,
  getCategoryIdByTitle,
  deleteTodo,
  deleteProject,
  deleteCategory,
};
