Feature: Delete a Todo
  As a student I want to delete a todo
  so that I can remove tasks I no longer need to track

  Background:
    Given the todo manager service is running

  Scenario Outline: Normal flow - Delete an existing todo
    Given a todo with title "<title>" and doneStatus false exists
    When a student deletes the todo with title "<title>"
    Then the deletion succeeds with status 200
    And the todo with title "<title>" no longer exists

    Examples:
      | title              |
      | Outdated reminder  |
      | Cancelled meeting  |
      | Wrong task entry   |

  Scenario Outline: Alternate flow - Delete a todo that belongs to a project
    Given a todo with title "<todoTitle>" and doneStatus false exists
    And a project with title "<projectTitle>" exists
    And the todo with title "<todoTitle>" is linked to the project with title "<projectTitle>"
    When a student deletes the todo with title "<todoTitle>"
    Then the deletion succeeds with status 200
    And the todo with title "<todoTitle>" no longer exists
    And the project with title "<projectTitle>" still exists

    Examples:
      | todoTitle   | projectTitle  |
      | Draft memo  | Work project  |

  Scenario Outline: Error flow - Attempt to delete an already deleted todo
    When a student deletes a todo with nonexistent id "<id>"
    Then the request fails with status 404

    Examples:
      | id    |
      | 99991 |
      | 99992 |
