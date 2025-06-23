# LinkToolkit Backend

[![Build Status](https://img.shields.io/github/actions/workflow/status/Sid-op777/linktoolkit-backend/build-and-test.yml?branch=main&style=for-the-badge)](https://github.com/Sid-op777/linktoolkit-backend/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

This repository contains the backend service for LinkToolkit, a modern and feature-rich URL shortening platform. The service is built with Spring Boot and provides a comprehensive REST API to support the [LinkToolkit frontend](https://linktoolkit.nx7.tech).

**Live Demo:**
*   **Frontend Application:** [linktoolkit.nx7.tech](https://linktoolkit.nx7.tech)

---

## Features

*   **URL Shortening:** Generate short, unique aliases for long URLs with optional custom aliases and configurable expiry dates.
*   **User Accounts:** Supports both anonymous and registered users. Anonymous links created within a session are automatically claimed upon registration.
*   **QR Code Generation:** Instantly generate and upload QR codes for any shortened link to Azure Blob Storage.
*   **Link Analytics:** Detailed analytics for each link, tracking total clicks, time-series data, referrers, devices, and geographic locations.
*   **Dual Authentication:** Secure access via both JWT for web sessions and persistent API Keys for developers.
*   **System Maintenance:** Includes an automated, scheduled job to clean up expired links from the database.

## Tech Stack & Architecture

This project is built with a modern, scalable, and containerized technology stack.

#### Core Technologies

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

#### Cloud & Deployment

![Render](https://img.shields.io/badge/Render-46E3B7?style=for-the-badge&logo=render&logoColor=white)
![Azure Blob Storage](https://img.shields.io/badge/Azure_Blob_Storage-0078D4?style=for-the-badge&logo=microsoftazure&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

#### Architecture

The system is designed with a clean separation of concerns, utilizing cloud services for scalability and resilience.

<img src="./flow.png" alt="Screenshot" width="500"/>


## API Documentation

The API is documented using the OpenAPI 3 standard.

*   **Public API Documentation (Redoc):** A clean, user-friendly view of the API is available at [**api.nx7.tech/docs**](https://api.nx7.tech/docs).
*   **Developer API Reference (Swagger UI):** While running locally, a Swagger UI instance is available for interactive API testing at `http://localhost:8080/swagger-ui.html`.

## Getting Started

Follow these instructions to get the project running on your local machine for development and testing using Docker.

### Prerequisites

*   **Docker & Docker Compose:** Ensure you have a recent version installed. Docker will handle the Java and Maven environment internally.
*   **Azure Account:** To obtain credentials for Azure Blob Storage.

### Local Development Setup

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/Sid-op777/linktoolkit-backend.git
    cd linktoolkit-backend
    ```
2.  **Create the environment file:**
    Create a new file named `.env` in the root of the project. This file will hold all your secrets and local configuration. **This file should never be committed to Git.**

    Copy the following content into your `.env` file and replace the placeholder values with your actual secrets.
    ```dotenv
    # .env file for LOCAL DEVELOPMENT

    # Server and App URLs
    SERVER_PORT=8080
    APP_BASE_URL=http://localhost:8080
    APP_FRONTEND_URL=http://localhost:3000

    # Local Docker Database Credentials
    SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/linktoolkit
    SPRING_DATASOURCE_USERNAME=user
    SPRING_DATASOURCE_PASSWORD=password
    SPRING_DATASOURCE_DB_NAME=linktoolkit # Used by docker-compose

    # Secrets
    JWT_SECRET=your-super-secret-long-and-secure-base64-encoded-key-here
    AZURE_STORAGE_ACCOUNT_NAME=your_azure_storage_account_name
    AZURE_STORAGE_ACCOUNT_KEY=your_azure_storage_account_key
    AZURE_STORAGE_ENDPOINT=https://your_account_name.blob.core.windows.net/
    ```
3.  **Run the application:**
    Use Docker Compose to build the application image and start both the Spring Boot app and PostgreSQL database containers.
    ```sh
    docker-compose up --build
    ```
    The application will be available at `http://localhost:8080`. The API is now ready to be tested with tools like Postman or cURL.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.