#!/bin/bash
# Ensure selenium network exists (harmless if already exists)
docker network create selenium_selenium-net 2>/dev/null || true

# Start application
DOCKER_BUILDKIT=0 docker compose -p java-selenium-cucumber-react up -d