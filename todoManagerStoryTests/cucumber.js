module.exports = {
  default: {
    paths: ["features/**/*.feature"],
    require: ["steps/**/*.js"],
    order: "random",
    format: ["progress-bar", "html:reports/cucumber-report.html"],
  },
};
