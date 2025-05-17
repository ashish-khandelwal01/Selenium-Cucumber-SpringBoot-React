Feature: Book Store Demo API Application
    As a user
    I want to use the Book Store APIs
    So that I can perform various actions

  @smoke_api
  Scenario: Get list of books using the API
    Given User send a GET request to the API endpoint "https://demoqa.com/BookStore/v1/Books"
    Then User should receive a response with status code 200

  @smoke_api
  Scenario Outline: Get a book using the API with specific ISBN
    Given User send a GET request to the API endpoint "https://demoqa.com/BookStore/v1/Book?ISBN=""<isbn>"
    Then User should receive a response with status code 200
    Examples:
        | isbn          |
        | 9781449325862 |
        | 9781449337711 |
