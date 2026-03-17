Feature: Update a Project
  As a student I want to update project details
  so that my project information stays accurate as work changes

  Background:
    Given the todo manager service is running

  Scenario Outline: Normal flow - Replace all project details with PUT
    Given a project with title "<originalTitle>", description "<originalDescription>", active <originalActive>, and completed <originalCompleted> exists
    When a student replaces the project with title "<originalTitle>" with title "<newTitle>", description "<newDescription>", active <newActive>, and completed <newCompleted>
    Then the response has status 200
    And the project with title "<newTitle>" has title "<newTitle>", description "<newDescription>", active <newActive>, and completed <newCompleted>

    Examples:
      | originalTitle   | originalDescription | originalActive | originalCompleted | newTitle          | newDescription         | newActive | newCompleted |
      | Semester plan   | Initial milestone   | true           | false             | Semester project  | Finalized milestone    | false     | true         |
      | App rewrite     | Rewrite prototype   | true           | false             | App launch        | Release-ready backlog  | true      | true         |

  Scenario Outline: Alternate flow - Mark an existing project as completed with POST
    Given a project with title "<title>", description "<description>", active <active>, and completed false exists
    When a student marks the project with title "<title>" as completed
    Then the response has status 200
    And the project with title "<title>" has title "<title>", description "<description>", active <active>, and completed true

    Examples:
      | title              | description              | active |
      | Reading tracker    | Track required readings  | true   |
      | Demo preparation   | Prepare the final demo   | false  |

  Scenario Outline: Error flow - Attempt to update a nonexistent project
    When a student updates a project with nonexistent id "<id>" with title "<title>"
    Then the request fails with status 404

    Examples:
      | id    | title            |
      | 99991 | Missing project  |
      | 99992 | Another missing  |
