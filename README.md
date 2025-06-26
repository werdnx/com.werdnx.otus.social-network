# Social Network Service

## Prerequisites

- Docker & Docker Compose
- Java 18 (for local build)
- Maven 3.8+

## Local Run

1. Clone the repo
2. Build & start services:
```bash
docker-compose up --build
```
3. The application will be accessible at http://localhost:8080

## API Endpoints

- `POST /login?id={id}&password={password}` - User authentication
- `POST /user/register` - Register new user (JSON body)
- `GET /user/get/{id}` - Retrieve user profile by ID

## Postman Collection

See `postman_collection.json`
