# Selenium-BDD-Cucumber-Framework

A full-stack automation framework combining Java Selenium, Cucumber (BDD), and a React-based dashboard for test reporting and management.

---

## Project Structure

```
java-selenium-cucumber-react/
│
├── backend/
│   └── automation-engine/   # Java Selenium + Cucumber backend
│
└── frontend/
    └── test-dashboard/      # React dashboard for test results
```

---

## Features

- **Selenium WebDriver** for browser automation
- **Cucumber BDD** for readable, maintainable test scenarios
- **Spring Boot** backend API for test orchestration
- **React Dashboard** for real-time test result visualization
- **REST API** for integration between backend and frontend
- **Docker** support for easy deployment (if configured)

---

## Prerequisites

- Java 11+
- Node.js 16+
- npm or yarn
- Chrome/Firefox browser (for Selenium)
- (Optional) Docker

---

## Backend Setup (Automation Engine)

1. Navigate to the backend directory:
   ```sh
   cd backend/automation-engine
   ```
2. Build the project:
   ```sh
   mvn clean install
   ```
3. Run the backend server:
   ```sh
   mvn spring-boot:run
   ```
4. The backend API will be available at `http://localhost:8080`.

---

## Frontend Setup (Test Dashboard)

1. Navigate to the frontend directory:
   ```sh
   cd frontend/test-dashboard
   ```
2. Install dependencies:
   ```sh
   npm install
   ```
3. Start the React app:
   ```sh
   npm start
   ```
4. The dashboard will be available at `http://localhost:3000`.

---

## Running Tests

- Use the dashboard to trigger and monitor test runs.
- Alternatively, run Cucumber tests directly from the backend:
  ```sh
  mvn test
  ```

---

## Running with Docker

You can run both the backend and frontend using Docker for easier setup and deployment.

### Prerequisites

- [Docker](https://www.docker.com/get-started) installed on your machine

### Steps

1. Build the Docker images for backend and frontend:
   ```sh
   docker-compose build
   ```
2. Start the services:
   ```sh
   docker-compose up
   ```
   This will start both the backend (Spring Boot) and frontend (React) containers.

3. Access the applications:
   - **Backend API:** [http://localhost:8080](http://localhost:8080)
   - **Frontend Dashboard:** [http://localhost:3000](http://localhost:3000)

4. To stop the services:
   ```sh
   docker-compose down
   ```

> **Note:**  
> Make sure you have a `docker-compose.yml` file in the project root. If not, you may need to create one to define the backend and frontend services.

---

## Configuration

- **WebDriver settings:**  
  Configure browser and driver options in `backend/automation-engine/src/main/resources/application.properties`.

- **API endpoints:**  
  The backend exposes REST endpoints for test management. See Swagger/OpenAPI docs if available.

---

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

---

## License

This project is licensed under the MIT License.

---

## Contact

For questions or support, please open an issue or contact the maintainer.

## UI Screenshots

Below are screenshots of the React dashboard UI:

![Dashboard Home](screenshot/dashboard.png)
![Reports](screenshot/reports.png)
![Test History](screenshot/test_history.png)
![Run Tests](screenshot/run_tests.png)

---