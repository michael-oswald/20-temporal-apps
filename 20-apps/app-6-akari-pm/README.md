# # 6/20 Temporal App - AkariPM

## ✅ Use Case
Dynamic project management tool, 2 goals: 1. clear view of where the project is at (all stakeholders can easily see), 2. simulate project impact of scope increasing in a project

## 🎥 YouTube Video

## ✨ Features
- Create and manage projects with tasks and tracks
- Assign workers to tasks with proper workflow management
- Reorder and move tasks between tracks
- Built with [Temporal](https://temporal.io/) for durable workflow orchestration
- Java + Spring Boot backend with DynamoDB persistence
- React frontend for intuitive project management

## 🛠 Technologies
- Java 17
- Spring Boot
- Temporal
- React
- Maven
- DynamoDB


# 🗂 Project Structure
src/main/java/ — Spring Boot backend and Temporal workflows
frontend/ — React frontend for seat booking
README.md — This file


## 🚀 Running Locally
### 1. Start Temporal Server
```sh
git clone https://github.com/temporalio/docker-compose.git
cd docker-compose
docker-compose up
```

### 2. Start Backend App
```
git clone https://github.com/michael-oswald/20-temporal-apps.git
cd 20-apps/app-6-akari-pm
./mvnw spring-boot:run
```

### 3. Start the Frontend
```
cd frontend
npm install
npm start
```

### 4. DynamoDB Setup
Need a DynamoDB table for confirmed bookings. You can create it using the AWS console or AWS CLI.
Table should be named `akaripm`


### Visit UI
http://localhost:3000/


