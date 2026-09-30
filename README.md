# QueueWise

QueueWise is a digital queue-management project for issuing customer tokens and managing a waiting queue.

## Tech stack

- Backend: Java, Spring Boot, Spring Data JPA
- Database: MySQL
- Frontend: React

## Project structure

- `queuewise-backend/queuewise` — Spring Boot application
- `queuewise-frontend` — React application

## Run locally

1. Start MySQL and create a database named `queuewise` if it does not already exist.
2. Configure the backend's local database connection and credentials. Keep your real password out of GitHub.
3. Open `queuewise-backend/queuewise` in IntelliJ IDEA and run `QueuewiseApplication`.
4. Open a terminal in `queuewise-frontend` and run:

   ```bash
   npm install
   npm run dev
   ```

5. Open the local address shown by the frontend terminal.