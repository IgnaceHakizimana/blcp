# Bank Licensing & Compliance Portal

This repository contains the source code for the Bank Licensing & Compliance Portal, an application designed to digitize the bank licensing process.

The project is divided into two main parts:
*   `backend-api`: A REST API built with Java, Spring Boot, and PostgreSQL.
*   `ui`: A web interface built with React, TypeScript, and Tailwind CSS.

Detailed explanation of the system's architecture and design trade-offs can be found in the `DESIGN_DOCUMENT.md`.

---

## Backend Setup & Execution

### Prerequisites
*   **Java 25**
*   **Maven 3.8+**
*   **Docker Desktop** (must be running for the test database to spin up)
*   **PostgreSQL** (must be running locally for the main application)

### How to Run the Backend
The backend requires a local PostgreSQL database to be running. You must create an empty database first before starting the application. 

By default, the application attempts to connect to `jdbc:postgresql://localhost:5432/blcp` using the username `postgres` and password `postgres`. If your setup is different, you must inject your credentials via environment variables.

1.  **Create the database:**
    Log into your local PostgreSQL instance and run:
    ```sql
    CREATE DATABASE blcp;
    ```
2.  **Navigate to the backend directory:**
    ```sh
    cd backend-api
    ```
3.  **Run the Spring Boot application:**
    Provide your full database connection details when running the command:
    ```sh
    DB_URL=jdbc:postgresql://localhost:5432/your_db_name DB_USERNAME=your_username DB_PASSWORD=your_password ./mvnw spring-boot:run
    ```
    
    The API will start on `http://localhost:8080`.

### How to Run the Backend Tests
The test suite uses Testcontainers and runs comprehensive integration tests for security boundaries, state transitions, and strict database constraints (like the append-only audit log trigger).

1.  **Navigate to the backend directory:**
    ```sh
    cd backend-api
    ```
2.  **Run the full test suite:**
    ```sh
    ./mvnw clean test
    ```

---

## Frontend Setup & Execution

### Prerequisites
*   **Node.js 20+**

### How to Run the Frontend
1.  **Navigate to the frontend directory:**
    ```sh
    cd ui
    ```
2.  **Install dependencies:**
    ```sh
    npm install
    ```
3.  **Start the development server:**
    ```sh
    npm run dev
    ```
    The application will be accessible at `http://localhost:5173`. 
    *(Note: Ensure the backend is also running).*

---

## Seed Data & Testing the system
On the first run against an empty database, the application will automatically seed the database with one user for each role and two sample applications. 

You can log in with the following credentials:

*   **Applicant:** `applicant@example.com` / `test123`
*   **Reviewer:** `reviewer@example.com` / `test123`
*   **Approver:** `approver@example.com` / `test123`

---

## API Documentation
The backend includes automated OpenAPI documentation via SpringDoc. 

With the Spring Boot application running, you can view the interactive Swagger UI and test all available endpoints directly from your browser by navigating to:
**http://localhost:8080/swagger-ui.html**
