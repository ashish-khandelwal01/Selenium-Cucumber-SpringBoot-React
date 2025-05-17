Feature:  Book Store Demo UI Application
  As a user
  I want to log in to the demo qa books application
  So that I can perform various actions

  Background:
    Given User navigate to the book store application login with url "https://demoqa.com"

  @smoke, @smoke_ui
  Scenario Outline: Login and validate incorrect credentials
    Given User log in to the application with username "<username>" and password "<password>"
    Then User should see an error message
    Examples:
      | username    | password    |
      | user1       | Password1   |
      | user2       | Password2   |

  @smoke
  Scenario Outline: Login and validate correct credentials
    Given User log in to the application with username "<username>" and password "<password>"
    Then User should see a search book field
    And User should logout from the application
    Examples:
      | username    | password     |
      | user3       | Password@1   |
      | user4       | Password@4   |

  @smoke
  Scenario Outline: Login and get a book
    Given User log in to the application with username "<username>" and password "<password>"
    Then User should see a search book field
    And User should see a list of books
    And User should logout from the application
    Examples:
      | username    | password     |
      | user3       | Password@1   |
      | user4       | Password@4   |