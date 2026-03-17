Feature: Add a Category to a Project
  As a student I want to add categories to a project
  so that I can group project work into clearer themes

  Background:
    Given the todo manager service is running

  Scenario Outline: Normal flow - Add a category to an existing project
    Given a project with title "<projectTitle>", description "<projectDescription>", active true, and completed false exists
    When a student creates a category with title "<categoryTitle>" and description "<categoryDescription>" under the project with title "<projectTitle>"
    Then the category creation succeeds
    And the category with title "<categoryTitle>" appears under the project with title "<projectTitle>"

    Examples:
      | projectTitle      | projectDescription        | categoryTitle   | categoryDescription    |
      | Thesis planning   | Organize thesis work      | Literature      | Papers and notes       |
      | Club website      | Improve the club website  | Design          | Visual assets          |

  Scenario Outline: Alternate flow - Add multiple categories to the same project
    Given a project with title "<projectTitle>", description "<projectDescription>", active true, and completed false exists
    When a student creates a category with title "<firstCategory>" and description "<firstDescription>" under the project with title "<projectTitle>"
    And a student creates a category with title "<secondCategory>" and description "<secondDescription>" under the project with title "<projectTitle>"
    Then the category with title "<firstCategory>" appears under the project with title "<projectTitle>"
    And the category with title "<secondCategory>" appears under the project with title "<projectTitle>"

    Examples:
      | projectTitle       | projectDescription      | firstCategory | firstDescription | secondCategory | secondDescription |
      | Course website     | Refresh course portal   | Content       | Page copy        | Testing        | Regression notes  |

  Scenario Outline: Error flow - Attempt to add a category to a nonexistent project
    When a student creates a category with title "<categoryTitle>" and description "<description>" under a project with id "<projectId>"
    Then the request fails with status 404

    Examples:
      | categoryTitle   | description           | projectId |
      | Missing parent  | Category with no home | 99991     |
      | Ghost category  | Another missing home  | 99992     |
