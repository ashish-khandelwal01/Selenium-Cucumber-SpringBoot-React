Feature:  Google UI Application
  As a user
  I want to go to google page
  So that I can perform search action

  Background:
    Given User wants to create a new browser instance
    And User navigate to google with url "https://duckduckgo.com/"

  @google
  Scenario Outline: Search and validate the search results
    Given User search for "<searchTerm>" in google search box
    Then User should see the search results page
    Examples:
      | searchTerm    |
      | cucumber bdd  |
      | selenium java |