Feature: Update a Todo
  As a student I want to update the details of a todo
  so that I can keep my task information current and accurate

  Background:
    Given the todo manager service is running

  Scenario Outline: Normal flow - Replace a todo's title and description using PUT
    Given a todo with title "<originalTitle>" and doneStatus false exists
    When a student replaces the todo with title "<originalTitle>" with title "<newTitle>" and description "<newDescription>"
    Then the response has status 200
    And the todo now has title "<newTitle>" and description "<newDescription>"

    Examples:
      | originalTitle | newTitle       | newDescription        |
      | Old task name | New task name  | Updated description   |
      | Draft title   | Final title    | Completed description |

  Scenario Outline: Alternate flow - Amend only the description of a todo using POST
    Given a todo with title "<title>" and doneStatus false exists
    When a student amends the description of the todo with title "<title>" to "<newDescription>"
    Then the response has status 200
    And the todo with title "<title>" has description "<newDescription>"

    Examples:
      | title          | newDescription              |
      | Research task  | Added more detail here      |
      | Planning task  | Updated the plan details    |

  Scenario Outline: Error flow - Attempt to update a nonexistent todo
    When a student updates a todo with nonexistent id "<id>" with title "<title>"
    Then the request fails with status 404

    Examples:
      | id    | title       |
      | 99991 | Will fail   |
      | 99992 | Also fails  |
