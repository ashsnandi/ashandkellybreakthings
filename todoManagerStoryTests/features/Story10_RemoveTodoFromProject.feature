Feature: Remove a Todo from a Project
  As a student I want to remove a linked todo from a project
  so that I can reorganize project scope without deleting the task itself

  Background:
    Given the todo manager service is running

  Scenario Outline: Normal flow - Remove a linked todo from a project
    Given a project with title "<projectTitle>", description "<projectDescription>", active true, and completed false exists
    And a todo with title "<todoTitle>" and doneStatus false exists
    And the todo with title "<todoTitle>" is linked to the project with title "<projectTitle>"
    When a student removes the todo with title "<todoTitle>" from the project with title "<projectTitle>"
    Then the response has status 200
    And the todo with title "<todoTitle>" does not appear in the tasks of project with title "<projectTitle>"
    And the todo with title "<todoTitle>" still exists

    Examples:
      | projectTitle        | projectDescription      | todoTitle           |
      | Personal dashboard  | Track dashboard work    | Fix widget layout   |
      | Research board      | Track paper tasks       | Summarize article   |

  Scenario Outline: Alternate flow - Remove one todo while keeping another linked to the same project
    Given a project with title "<projectTitle>", description "<projectDescription>", active true, and completed false exists
    And a todo with title "<firstTodo>" and doneStatus false exists
    And a todo with title "<secondTodo>" and doneStatus false exists
    And the todo with title "<firstTodo>" is linked to the project with title "<projectTitle>"
    And the todo with title "<secondTodo>" is linked to the project with title "<projectTitle>"
    When a student removes the todo with title "<firstTodo>" from the project with title "<projectTitle>"
    Then the response has status 200
    And the todo with title "<firstTodo>" does not appear in the tasks of project with title "<projectTitle>"
    And the todo with title "<secondTodo>" appears in the tasks of project with title "<projectTitle>"

    Examples:
      | projectTitle    | projectDescription    | firstTodo         | secondTodo       |
      | API cleanup     | Refine the API scope  | Remove old route  | Update examples  |

  Scenario Outline: Error flow - Attempt to remove the same todo twice from a project
    Given a project with title "<projectTitle>", description "<projectDescription>", active true, and completed false exists
    And a todo with title "<todoTitle>" and doneStatus false exists
    And the todo with title "<todoTitle>" is linked to the project with title "<projectTitle>"
    When a student removes the todo with title "<todoTitle>" from the project with title "<projectTitle>"
    And a student removes the todo with title "<todoTitle>" from the project with title "<projectTitle>"
    Then the request fails with status 404

    Examples:
      | projectTitle        | projectDescription      | todoTitle         |
      | Refactor roadmap    | Refactor sequencing     | Drop old helper   |
      | Deployment queue    | Deployment steps        | Remove temp task  |
