Feature: Mark a Todo as Done
  As a student I want to mark a todo as done
  so that I can track which tasks I have completed

  Background:
    Given the todo manager service is running

  Scenario Outline: Normal flow - Mark an active todo as done
    Given a todo with title "<title>" and doneStatus false exists
    When a student marks the todo with title "<title>" as done
    Then the todo with title "<title>" has doneStatus "true"

    Examples:
      | title                |
      | Submit assignment    |
      | Pay electricity bill |
      | Water the plants     |

  Scenario Outline: Alternate flow - Mark a done todo as not done
    Given a todo with title "<title>" and doneStatus true exists
    When a student marks the todo with title "<title>" as not done
    Then the todo with title "<title>" has doneStatus "false"

    Examples:
      | title             |
      | Review notes      |
      | Buy new notebook  |

  Scenario Outline: Error flow - Attempt to mark a nonexistent todo as done
    When a student marks the todo with id "<nonExistentId>" as done
    Then the request fails with status 404

    Examples:
      | nonExistentId |
      | 99991         |
      | 99992         |
