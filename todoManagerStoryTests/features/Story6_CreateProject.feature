Feature: Create a Project
  As a student I want to create a project
  so that I can organize related tasks under one piece of work

  Background:
    Given the todo manager service is running

  Scenario Outline: Normal flow - Create a project with full details
    When a student creates a project with title "<title>", description "<description>", active <active>, and completed <completed>
    Then the project creation succeeds
    And the created project has title "<title>", description "<description>", active <active>, and completed <completed>

    Examples:
      | title              | description                    | active | completed |
      | Capstone project   | Build the final term project   | true   | false     |
      | Internship search  | Track company applications     | false  | false     |

  Scenario Outline: Alternate flow - Create a project with title only
    When a student creates a project with title "<title>" only
    Then the project creation succeeds
    And the created project keeps title "<title>" with default active false and completed false

    Examples:
      | title             |
      | Weekend errands   |
      | Research backlog  |

  Scenario Outline: Error flow - Attempt to create a project with an invalid active value
    When a student creates a project with title "<title>" and invalid active value "<activeValue>"
    Then the request fails with a client error status
    And the response contains an error message

    Examples:
      | title             | activeValue |
      | Broken project    | yes please  |
      | Invalid booleans  | definitely  |
