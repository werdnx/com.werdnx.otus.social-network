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

## Default User

A default user is created via the database migration scripts:

- **ID**: `1`
- **First Name**: `Admin`
- **Last Name**: `User`
- **Password**: `secret` (use this to obtain a token)

## Authentication

All protected endpoints require a JWT Bearer token. First, log in to receive a token:

```bash
POST /login?id=1&password=secret
```

Example Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzUxMDU3NTY3LCJleHAiOjE3NTEwNTg0Njd9.kkdqsyB-pIRRGLDxhxM8mO4d-LaDgqUxdvOUu9qaYF4",
  "expiresIn": 900,
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzUxMDU3NTY3LCJleHAiOjE3NTE2NjIzNjd9.dnUfEgpIzlUr3dc9yNJzeOsXkN7-BMmsXP3RmferrHw",
  "tokenType": "Bearer"
}
```

## API Endpoints

- `POST /login?id={id}&password={password}`  
  Obtain an authentication token.

- `POST /user/register`  
  Register a new user.  
  **Headers**:
  ```
  Authorization: Bearer <accessToken>
  ```  
  **Body** (JSON):
  ```json
  {
    "firstName": "Jane",
    "lastName": "Doe",
    "birthDate": "1990-01-01",
    "gender": "Female",
    "interests": "Reading,Travel",
    "city": "Moscow",
    "passwordHash": "secure_password"
  }
  ```

- `GET /user/get/{id}`  
  Retrieve a user profile by ID.  
  **Headers**:
  ```
  Authorization: Bearer <accessToken>
  ```

## Postman Collection

See `postman_collection.json`  
