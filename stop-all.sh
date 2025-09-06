#!/bin/bash
# Stop application
docker compose -p java-selenium-cucumber-react down

# Stop Selenium
docker compose -f docker-compose.selenium.yml -p java-selenium-cucumber-react down