Feature: Assign a Todo to a Project
  As a student I want to assign a todo to a project
  so that I can organize my tasks by project

  Background:
    Given the todo manager service is running

  Scenario Outline: Normal flow - Assign a todo to a project
    Given a todo with title "<todoTitle>" and doneStatus false exists
    And a project with title "<projectTitle>" exists
    When a student assigns the todo with title "<todoTitle>" to the project with title "<projectTitle>"
    Then the assignment succeeds with status 201
    And the todo with title "<todoTitle>" appears in the tasks of project with title "<projectTitle>"

    Examples:
      | todoTitle       | projectTitle     |
      | Buy textbooks   | School semester  |
      | Fix the bug     | Software project |

  Scenario Outline: Alternate flow - Assign multiple todos to the same project
    Given a todo with title "<todoTitle1>" and doneStatus false exists
    And a todo with title "<todoTitle2>" and doneStatus false exists
    And a project with title "<projectTitle>" exists
    When a student assigns the todo with title "<todoTitle1>" to the project with title "<projectTitle>"
    And a student assigns the todo with title "<todoTitle2>" to the project with title "<projectTitle>"
    Then the todo with title "<todoTitle1>" appears in the tasks of project with title "<projectTitle>"
    And the todo with title "<todoTitle2>" appears in the tasks of project with title "<projectTitle>"

    Examples:
      | todoTitle1   | todoTitle2  | projectTitle |
      | Write report | Edit report | Fall project |

  Scenario Outline: Error flow - Attempt to assign a todo to a nonexistent project
    Given a todo with title "<todoTitle>" and doneStatus false exists
    When a student assigns the todo with title "<todoTitle>" to a project with id "<nonExistentId>"
    Then the request fails with status 404

    Examples:
      | todoTitle    | nonExistentId |
      | Random task  | 99991         |
      | Another task | 99992         |
