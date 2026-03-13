const { Before, After, Given } = require("@cucumber/cucumber");
const { expect } = require("chai");
const utils = require("../TestUtil.js");

Before(async function () {
  this.initialTodoIds = new Set(await utils.getAllTodoIds());
  this.initialProjectIds = new Set(await utils.getAllProjectIds());
});

After(async function () {
  const currentTodoIds = await utils.getAllTodoIds();
  for (const id of currentTodoIds) {
    if (!this.initialTodoIds.has(id)) {
      await utils.deleteTodo(id);
    }
  }

  const currentProjectIds = await utils.getAllProjectIds();
  for (const id of currentProjectIds) {
    if (!this.initialProjectIds.has(id)) {
      await utils.deleteProject(id);
    }
  }
});

Given("the todo manager service is running", async function () {
  const running = await utils.isServiceRunning();
  expect(running, "Todo manager service must be running on localhost:4567").to.be.true;
});
