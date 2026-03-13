Feature: Create a Todo
  As a student I want to create a new todo with a title and description
  so that I can track my tasks with relevant details

  Background:
    Given the todo manager service is running

  Scenario Outline: Normal flow - Create a todo with title and description
    When a student creates a todo with title "<title>" and description "<description>"
    Then the todo is created with status 201
    And the created todo has title "<title>" and description "<description>"

    Examples:
      | title              | description                  |
      | Buy groceries      | Get milk eggs and bread      |
      | Study for midterm  | Review chapters one to five  |
      | Call the dentist   | Book a cleaning appointment  |

  Scenario Outline: Alternate flow - Create a todo with title only
    When a student creates a todo with title "<title>" and no description
    Then the todo is created with status 201
    And the created todo has an empty description

    Examples:
      | title              |
      | Quick note         |
      | Follow up email    |
      | Read the news      |

  Scenario Outline: Error flow - Attempt to create a todo with an empty title
    When a student creates a todo with title "" and description "<description>"
    Then the request fails with status 400
    And the response contains an error message

    Examples:
      | description          |
      | missing title here   |
      | another bad attempt  |
