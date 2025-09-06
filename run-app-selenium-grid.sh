#!/bin/bash

docker network create selenium_selenium-net 2>/dev/null || true

docker compose -f docker-compose.selenium.yml -p java-selenium-cucumber-react up -d

# Start app after
docker compose -f docker-compose.yml -p java-selenium-cucumber-react up -d