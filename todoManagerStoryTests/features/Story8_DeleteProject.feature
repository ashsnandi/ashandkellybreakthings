Feature: Delete a Project
  As a student I want to delete a project
  so that I can remove workspaces that are no longer relevant

  Background:
    Given the todo manager service is running

  Scenario Outline: Normal flow - Delete an existing project
    Given a project with title "<title>", description "<description>", active <active>, and completed <completed> exists
    When a student deletes the project with title "<title>"
    Then the project deletion succeeds with status 200
    And the project with title "<title>" no longer exists

    Examples:
      | title             | description              | active | completed |
      | Old lab project   | Obsolete lab workspace   | true   | false     |
      | Travel planning   | Past trip checklist      | false  | true      |

  Scenario Outline: Alternate flow - Delete a project without deleting its linked todo
    Given a project with title "<projectTitle>", description "<description>", active true, and completed false exists
    And a todo with title "<todoTitle>" and doneStatus false exists
    And the todo with title "<todoTitle>" is linked to the project with title "<projectTitle>"
    When a student deletes the project with title "<projectTitle>"
    Then the project deletion succeeds with status 200
    And the project with title "<projectTitle>" no longer exists
    And the todo with title "<todoTitle>" still exists

    Examples:
      | projectTitle       | description                | todoTitle           |
      | Group presentation | Slides and practice notes  | Practice speaking   |
      | Work archive       | Archived work items        | Save reference docs |

  Scenario Outline: Error flow - Attempt to delete a nonexistent project
    When a student deletes a project with nonexistent id "<id>"
    Then the request fails with status 404

    Examples:
      | id    |
      | 99991 |
      | 99992 |
