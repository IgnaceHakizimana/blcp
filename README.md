# Bank Licensing & Compliance Portal

This repository contains the source code for the Bank Licensing & Compliance Portal, a full-stack application designed to digitize and streamline the bank licensing process.

The project is divided into two main parts:
*   `backend-api`: A secure REST API built with Java/Spring Boot.
*   `ui`: A functional web interface built with React and TypeScript.

A comprehensive explanation of the system's architecture, data model, and design trade-offs can be found in the `DESIGN_DOCUMENT.md`.

---

## Backend Setup & Execution

### Prerequisites
*   **Java 25**
*   **Maven 3.8+**
*   **Docker Desktop** (must be running)

### How to Run the Backend
The backend is configured to use **Testcontainers**, which will automatically spin up a temporary, isolated PostgreSQL database in Docker for both running the application and executing tests.

**No manual database setup is required.**

1.  **Navigate to the backend directory:**
    
