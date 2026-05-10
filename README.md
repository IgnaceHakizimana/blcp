# Bank Licensing & Compliance Portal

This repository contains the source code for the Bank Licensing & Compliance Portal, an application designed to digitize the bank licensing process.

The project is divided into two main parts:
*   `backend-api`: A REST API built with Java, Spring Boot, and Postgres.
*   `ui`: A web interface built with React, TypeScript, and Tailwind CSS.

Detailed explanation of the system's architecture and design trade-offs can be found in the `DESIGN_DOCUMENT.md`.

---

## Backend Setup & Execution

### Prerequisites
*   **Java 25**
*   **Maven 3.8+**
*   **Docker Desktop** (must be running for the database to spin up)

### How to Run the Backend
The backend is configured to use **Testcontainers**. This means it will automatically spin up a temporary, isolated PostgreSQL database inside Docker for both running the application and executing tests.

**No manual database setup is required.**

1.  **Navigate to the backend directory:**
    ```sh
    cd backend-api
    ```
2.  **Run the Spring Boot application:**
    ```sh
    ./mvnw spring-boot:run
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
    *(Note: Ensure the Spring Boot backend is also running, as the Vite server automatically proxies `/api` requests to port 8080).*

---

## Seed Data & Testing the UI
On the first run against an empty database, the application will automatically seed the database with one user for each role and two sample applications. 

You can log into the React UI (`http://localhost:5173/login`) with the following credentials to test the role-based routing and state machine:

*   **Applicant:** `applicant@example.com` / `test123`
*   **Reviewer:** `reviewer@example.com` / `test123`
*   **Approver:** `approver@example.com` / `test123`

---

## API Documentation
The backend includes automated OpenAPI documentation via SpringDoc. 

With the Spring Boot application running, you can view the interactive Swagger UI and test all available endpoints directly from your browser by navigating to:
**http://localhost:8080/swagger-ui.html**
