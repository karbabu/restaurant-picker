# Restaurant Picker - Microservices Application

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A production-ready microservices application that helps teams collaboratively decide where to go for lunch using real-time notifications and random restaurant selection.

##  Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [WebSocket Notifications](#websocket-notifications)
- [Testing Guide](#testing-guide)
- [Configuration](#configuration)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

##  Overview

**Restaurant Picker** is a distributed system built with microservices architecture that solves the common problem of team lunch decision-making. Users can create sessions, invite team members, submit restaurant suggestions, and let the system randomly select the winner.

### Key Highlights

-  **Microservices Architecture** - Scalable, maintainable service design
-  **Real-time Notifications** - WebSocket-based instant updates via 3 channels
-  **RESTful APIs** - Complete Swagger/OpenAPI documentation
-  **Event-Driven** - Personal, session, and global notification streams
-  **Spring Batch Integration** - Automated data loading on startup
-  **Global Error Handling** - Consistent error responses across services
-  **Docker Support** - One-command deployment
-  **Interactive UI** - Built-in web interface for testing

##  Features

### Core Functionality

- **Session Management**
  - Create lunch decision sessions
  - Invite team members to join
  - Role-based permissions (only authorized users can initiate)
  
- **Restaurant Submission**
  - Submit restaurant suggestions with details
  - View all submissions in real-time
  - Input validation and sanitization
  
- **Random Selection**
  - Fair random restaurant picker
  - Session end by initiator only
  - Prevent joining ended sessions
  
- **Real-time Notifications** (3 Channels)
  - Personal notifications (`/user/{userId}/queue/notifications`)
  - Session updates (`/topic/session/{sessionId}`)
  - Global announcements (`/topic/global`)

### Technical Features

- RESTful API with comprehensive Swagger documentation
- WebSocket/STOMP protocol for real-time communication
- Spring Batch for CSV data import
- PostgreSQL database with JPA/Hibernate
- API Gateway for unified routing
- Global exception handling
- Docker Compose orchestration

##  Architecture

### System Architecture
```
+-------------------------------------------------------------+
¦                     CLIENT LAYER                            ¦
¦  Web Browser | Mobile App | Postman/Swagger UI              ¦
+-------------------------------------------------------------+
                        ¦
                        ¦
+-------------------------------------------------------------+
¦                  API GATEWAY (:8080)                        ¦
¦  • Routing          • Error Handling                        ¦
¦  • Swagger UI       • Static Frontend                       ¦
+-------------------------------------------------------------+
            ¦                     ¦
    +-------¦--------+    +------¦----------+
    ¦  User Service  ¦    ¦ Session Service ¦
    ¦     :8081      ¦    ¦      :8082      ¦
    ¦                ¦    ¦                 ¦
    ¦ • User CRUD    ¦    ¦ • Session CRUD  ¦
    ¦ • Spring Batch ¦    ¦ • Restaurants   ¦
    ¦ • Auth Check   ¦    ¦ • WebSocket     ¦
    +----------------+    +-----------------+
            ¦                    ¦
            +--------------------+
                       ¦
              +----------------+
              ¦   PostgreSQL   ¦
              ¦     :5432      ¦
              +----------------+
```

### Notification Architecture
```
Client                    WebSocket Server              Channels
  ¦                             ¦
  ¦---- Connect to /ws -------->¦
  ¦                             ¦
  ¦---- Subscribe ------------->¦
  ¦                             ¦
  +--> /user/{id}/queue/--------¦  Personal (Invites)
  ¦     notifications           ¦
  ¦                             ¦
  +--> /topic/session/{id} -----¦  Session (Updates)
  ¦                             ¦
  +--> /topic/global -----------¦  Global (Announcements)
  ¦                             ¦
  ¦<---- Notifications ---------¦
```

##  Technology Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.2.0** - Application framework
- **Spring Cloud Gateway** - API gateway
- **Spring Data JPA** - Database access
- **Spring WebSocket** - Real-time communication
- **Spring Batch** - Data processing
- **PostgreSQL 15** - Relational database
- **Maven** - Build tool
- **Lombok** - Boilerplate reduction

### Frontend
- **HTML5/CSS3/JavaScript** - Web interface
- **SockJS** - WebSocket fallback
- **STOMP.js** - WebSocket protocol
- **Fetch API** - HTTP requests

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Orchestration
- **Swagger/OpenAPI 3.0** - API documentation

##  Prerequisites

Ensure you have the following installed:

| Software | Version | Command to Check |
|----------|---------|------------------|
| Java JDK | 17+ | `java -version` |
| Maven | 3.6+ | `mvn -version` |
| Docker | 20.10+ | `docker --version` |
| Docker Compose | 2.0+ | `docker-compose --version` |
| Git | 2.0+ | `git --version` |

### Installation Links

- [Java 17](https://adoptium.net/)
- [Maven](https://maven.apache.org/download.cgi)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Git](https://git-scm.com/downloads)

##  Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/karbabu/restaurant-picker.git
cd restaurant-picker
```

### 2. Start Application (Docker)
```bash
# Build and start all services
docker-compose up --build -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f
```

### 3. Verify Installation
```bash
# Check API Gateway
curl http://localhost:8080/actuator/health

# Get all users
curl http://localhost:8080/api/users | jq

# Expected output: List of 5 pre-loaded users
```

### 4. Access Applications

| Application | URL | Description |
|-------------|-----|-------------|
| Frontend UI | http://localhost:8080/index.html | Interactive web interface |
| Swagger UI | http://localhost:8080/swagger-ui.html | API documentation |
| API Gateway | http://localhost:8080 | Main entry point |
| User Service | http://localhost:8081 | Direct user API |
| Session Service | http://localhost:8082 | Direct session API |

##  API Documentation

### Swagger UI

Access comprehensive API documentation:
```
Main Swagger:      http://localhost:8080/swagger-ui.html
User Service:      http://localhost:8081/swagger-ui.html
Session Service:   http://localhost:8082/swagger-ui.html
```

### Quick API Reference

#### User Service
```bash
# Get all users
GET /api/users

# Get user by ID
GET /api/users/{userId}

# Check if user can initiate sessions
GET /api/users/{userId}/can-initiate
```

#### Session Service
```bash
# Create session
POST /api/sessions
Body: {"invitedUserIds": ["user-id-1", "user-id-2"]}

# Join session
POST /api/sessions/{sessionId}/join
Body: {"userId": "user-id"}

# Submit restaurant
POST /api/sessions/{sessionId}/submit
Body: {
  "restaurantName": "Pizza Paradise",
  "address": "123 Main St",
  "description": "Best pizza",
  "submittedByUserId": "user-id"
}

# Get session details
GET /api/sessions/{sessionId}

# Get all active sessions
GET /api/sessions

# End session (pick winner)
POST /api/sessions/{sessionId}/end
Body: {"userId": "initiator-user-id"}
```

### Complete API Testing Example
```bash
#!/bin/bash

# 1. Get users
USERS=$(curl -s http://localhost:8080/api/users)
USER_A=$(echo $USERS | jq -r '.[0].userId')
USER_B=$(echo $USERS | jq -r '.[1].userId')

# 2. Create session
SESSION=$(curl -s -X POST http://localhost:8080/api/sessions \
  -H "Content-Type: application/json" \
  -d "{\"invitedUserIds\": [\"$USER_A\"]}")
SESSION_ID=$(echo $SESSION | jq -r '.sessionId')

echo "Session created: $SESSION_ID"

# 3. User B joins
curl -s -X POST http://localhost:8080/api/sessions/$SESSION_ID/join \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_B\"}" | jq

# 4. Both submit restaurants
curl -s -X POST http://localhost:8080/api/sessions/$SESSION_ID/submit \
  -H "Content-Type: application/json" \
  -d "{
    \"restaurantName\": \"Pizza Paradise\",
    \"address\": \"123 Main St\",
    \"description\": \"Best pizza\",
    \"submittedByUserId\": \"$USER_A\"
  }" | jq

curl -s -X POST http://localhost:8080/api/sessions/$SESSION_ID/submit \
  -H "Content-Type: application/json" \
  -d "{
    \"restaurantName\": \"Burger Haven\",
    \"address\": \"456 Oak Ave\",
    \"description\": \"Great burgers\",
    \"submittedByUserId\": \"$USER_B\"
  }" | jq

# 5. View submissions
curl -s http://localhost:8080/api/sessions/$SESSION_ID/submissions | jq

# 6. End session and pick winner
curl -s -X POST http://localhost:8080/api/sessions/$SESSION_ID/end \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_A\"}" | jq

echo "Winner selected!"
```

##  WebSocket Notifications

### Three Notification Channels

The application provides real-time updates through three distinct WebSocket channels:

#### 1. Personal Notifications
**Channel:** `/user/{userId}/queue/notifications`

**Purpose:** User-specific messages (invitations, personal alerts)

**Events:**
- `INVITATION_RECEIVED` - Invited to a session
- `SESSION_ENDED` - Your session ended with results

#### 2. Session Notifications
**Channel:** `/topic/session/{sessionId}`

**Purpose:** Session-specific updates for all participants

**Events:**
- `USER_JOINED` - User joined the session
- `RESTAURANT_SUBMITTED` - New restaurant suggested
- `SESSION_ENDED` - Session ended, winner announced

#### 3. Global Notifications
**Channel:** `/topic/global`

**Purpose:** System-wide announcements to all connected users

**Events:**
- `SESSION_CREATED` - New session available
- `SYSTEM_ANNOUNCEMENT` - Admin messages

### JavaScript WebSocket Example
```javascript
// Connect to WebSocket
const socket = new SockJS('http://localhost:8082/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to personal notifications
    stompClient.subscribe('/user/' + userId + '/queue/notifications', 
        function(message) {
            const notification = JSON.parse(message.body);
            console.log('Personal:', notification.message);
        }
    );
    
    // Subscribe to session updates
    stompClient.subscribe('/topic/session/' + sessionId, 
        function(message) {
            const notification = JSON.parse(message.body);
            console.log('Session:', notification.message);
        }
    );
    
    // Subscribe to global announcements
    stompClient.subscribe('/topic/global', 
        function(message) {
            const notification = JSON.parse(message.body);
            console.log('Global:', notification.message);
        }
    );
});
```

### Testing Notifications

#### Using the Web Interface

1. Open http://localhost:8080/index.html
2. Select a user and click "Connect"
3. Open browser console (F12)
4. Watch notifications appear in real-time

#### Using cURL
```bash
# Test personal notification
curl -X POST http://localhost:8082/api/notifications/test/personal/{userId}

# Test session notification
curl -X POST http://localhost:8082/api/notifications/test/session/{sessionId}

# Test global notification
curl -X POST http://localhost:8082/api/notifications/test/global
```

#### Multi-User Testing

1. Open two browser windows (one incognito)
2. Window 1: Select user "john.doe", connect, create session
3. Window 2: Select user "jane.smith", connect, join session
4. Submit restaurants and watch real-time updates in both windows
5. End session and see winner announcement

##  Testing Guide

### Manual Testing Workflow

#### Scenario: Complete Lunch Decision Flow
```bash
# Step 1: Get available users
curl http://localhost:8080/api/users

# Step 2: User A creates session
curl -X POST http://localhost:8080/api/sessions \
  -H "Content-Type: application/json" \
  -d '{"invitedUserIds": ["user-a-id"]}'
# Note the sessionId from response

# Step 3: User B joins session
curl -X POST http://localhost:8080/api/sessions/{sessionId}/join \
  -H "Content-Type: application/json" \
  -d '{"userId": "user-b-id"}'

# Step 4: Both users submit restaurants
curl -X POST http://localhost:8080/api/sessions/{sessionId}/submit \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantName": "Restaurant A",
    "address": "Address A",
    "description": "Description A",
    "submittedByUserId": "user-a-id"
  }'

curl -X POST http://localhost:8080/api/sessions/{sessionId}/submit \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantName": "Restaurant B",
    "address": "Address B",
    "description": "Description B",
    "submittedByUserId": "user-b-id"
  }'

# Step 5: View all submissions
curl http://localhost:8080/api/sessions/{sessionId}/submissions

# Step 6: End session and select winner
curl -X POST http://localhost:8080/api/sessions/{sessionId}/end \
  -H "Content-Type: application/json" \
  -d '{"userId": "user-a-id"}'

# Step 7: Verify cannot join ended session (should fail)
curl -X POST http://localhost:8080/api/sessions/{sessionId}/join \
  -H "Content-Type: application/json" \
  -d '{"userId": "user-c-id"}'
```



##  Configuration

### Environment Variables

Configure services using environment variables in `docker-compose.yml`:
```yaml
environment:
  # Database
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/restaurant_picker
  SPRING_DATASOURCE_USERNAME: postgres
  SPRING_DATASOURCE_PASSWORD: postgres
  
  # Service URLs
  USER_SERVICE_URL: http://user-service:8081
  
  # Logging
  LOGGING_LEVEL_COM_CAPGEMINI: DEBUG
  
  # Spring Profiles
  SPRING_PROFILES_ACTIVE: production
```

### Application Properties

#### API Gateway (`api-gateway/src/main/resources/application.yml`)
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://user-service:8081
          predicates:
            - Path=/api/users/**
        
        - id: session-service
          uri: http://session-service:8082
          predicates:
            - Path=/api/sessions/**
```

#### User Service (`user-service/src/main/resources/application.yml`)
```yaml
server:
  port: 8081

spring:
  application:
    name: user-service
  datasource:
    url: jdbc:postgresql://localhost:5432/restaurant_picker
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
  batch:
    job:
      enabled: true  # Auto-run CSV import on startup
```

#### Session Service (`session-service/src/main/resources/application.yml`)
```yaml
server:
  port: 8082

spring:
  application:
    name: session-service
  datasource:
    url: jdbc:postgresql://localhost:5432/restaurant_picker
    username: postgres
    password: postgres

user:
  service:
    url: http://user-service:8081
```

### Pre-loaded Users

Users are automatically loaded from `user-service/src/main/resources/users.csv`:

| Username | Email | Can Initiate Sessions |
|----------|-------|----------------------|
| john.doe | john.doe@company.com |  Yes |
| jane.smith | jane.smith@company.com |  Yes |
| bob.wilson | bob.wilson@company.com |  No |
| alice.brown | alice.brown@company.com |  Yes |
| charlie.davis | charlie.davis@company.com |  No |

To add more users, edit the CSV file and restart the user-service.

##  Deployment

### Docker Compose (Production)
```bash
# Build production images
docker-compose -f docker-compose.prod.yml build

# Start services
docker-compose -f docker-compose.prod.yml up -d

# Scale services
docker-compose -f docker-compose.prod.yml up -d --scale session-service=3
```

### Kubernetes Deployment

Example `k8s/deployment.yaml`:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 2
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: restaurant-picker/api-gateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
```

Deploy:
```bash
kubectl apply -f k8s/
kubectl get pods
kubectl get services
```

### CI/CD Pipeline (GitHub Actions)

`.github/workflows/deploy.yml`:
```yaml
name: Build and Deploy

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'adopt'
    
    - name: Build with Maven
      run: mvn clean install -DskipTests
    
    - name: Build Docker images
      run: docker-compose build
    
    - name: Run tests
      run: mvn test
    
    - name: Push to Docker Hub
      run: |
        echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
        docker-compose push
```

##  Troubleshooting

### Common Issues

#### 1. Services Not Starting
```bash
# Check logs
docker-compose logs api-gateway
docker-compose logs user-service
docker-compose logs session-service

# Common causes:
# - Port already in use
# - Database not ready
# - Missing environment variables

# Solution: Clean restart
docker-compose down -v
docker-compose up -d --build
```

#### 2. Database Connection Failed
```bash
# Check PostgreSQL status
docker-compose ps postgres

# Check database exists
docker exec -it restaurant-picker-db psql -U postgres -l

# Recreate database
docker-compose down -v
docker volume rm restaurant-picker_postgres_data
docker-compose up -d
```

#### 3. Users Not Loaded
```bash
# Check if users.csv exists
ls -la user-service/src/main/resources/users.csv

# Check Spring Batch logs
docker-compose logs user-service | grep -i batch

# Manually check database
docker exec -it restaurant-picker-db psql -U postgres -d restaurant_picker
# Then: SELECT * FROM users;
```

#### 4. WebSocket Connection Failed
```bash
# Check session-service is running
curl http://localhost:8082/actuator/health

# Check WebSocket endpoint
wscat -c ws://localhost:8082/ws

# Check CORS configuration
# Ensure session-service application.yml has:
# spring.websocket.allowed-origins: "*"
```

#### 5. Swagger UI Not Loading
```bash
# Check SpringDoc dependency in pom.xml
# Verify route in API Gateway

# Test direct access
curl http://localhost:8081/v3/api-docs
curl http://localhost:8082/v3/api-docs

# Rebuild API Gateway
docker-compose build api-gateway
docker-compose up -d api-gateway
```

### Health Checks
```bash
# Check all services
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health

# Check Gateway routes
curl http://localhost:8080/actuator/gateway/routes | jq

# Check database connection
docker exec -it restaurant-picker-db pg_isready -U postgres
```

### Log Analysis
```bash
# View all logs
docker-compose logs -f

# Filter by service
docker-compose logs -f session-service

# Search for errors
docker-compose logs | grep -i error

# Follow last 100 lines
docker-compose logs -f --tail=100

# Export logs to file
docker-compose logs > application.log
```

##  Monitoring & Metrics

### Actuator Endpoints

Available metrics at `/actuator`:
```bash
# Health status
curl http://localhost:8080/actuator/health

# Application info
curl http://localhost:8080/actuator/info

# Metrics
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### Prometheus Integration

Add to `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus,health,info,metrics
  metrics:
    export:
      prometheus:
        enabled: true
```

Access metrics:
```
http://localhost:8080/actuator/prometheus
```

##  Contributing

We welcome contributions! Please follow these steps:

### 1. Fork the Repository
```bash
git clone https://github.com/karbabu/restaurant-picker.git
cd restaurant-picker
```

### 2. Create a Feature Branch
```bash
git checkout -b feature/amazing-feature
```

### 3. Make Changes

- Follow existing code style
- Add tests for new features
- Update documentation

### 4. Commit Changes
```bash
git add .
git commit -m "Add amazing feature"
```

### 5. Push and Create Pull Request
```bash
git push origin feature/amazing-feature
```

Then open a pull request on GitHub.

### Code Style Guidelines

- Use Java 17 features
- Follow Spring Boot best practices
- Write meaningful commit messages
- Add JavaDoc for public methods
- Include unit tests (target: 80% coverage)

##  License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
```
Copyright 2026 Restaurant Picker Team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

##  Support

### Getting Help

- **Documentation:** [GitHub Wiki](https://github.com/your-org/restaurant-picker/wiki)
- **Issues:** [GitHub Issues](https://github.com/your-org/restaurant-picker/issues)
- **Email:** support@restaurantpicker.com
- **Slack:** [Join our Slack](https://slack.restaurantpicker.com)

### Reporting Bugs

Please include:
- Operating system and version
- Java version (`java -version`)
- Docker version (`docker --version`)
- Steps to reproduce
- Expected vs actual behavior
- Log files (`docker-compose logs`)

##  Roadmap

### Version 1.1 (Q2 2026)
- [ ] User authentication with JWT
- [ ] Mobile app (React Native)
- [ ] Email notifications
- [ ] Restaurant rating system
- [ ] Session history

### Version 1.2 (Q3 2026)
- [ ] Multiple restaurant categories
- [ ] Voting system (not just random)
- [ ] Integration with Google Maps
- [ ] Dietary restrictions filter
- [ ] Team preferences learning

### Version 2.0 (Q4 2026)
- [ ] Machine learning recommendations
- [ ] Slack/Teams integration
- [ ] Calendar integration
- [ ] Advanced analytics dashboard
- [ ] Multi-tenancy support

##  Acknowledgments

- Spring Boot team for the excellent framework
- Docker for containerization platform
- PostgreSQL for reliable database
- Swagger for API documentation
- All contributors who helped build this project

##  Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [WebSocket Protocol](https://datatracker.ietf.org/doc/html/rfc6455)
- [STOMP Protocol](https://stomp.github.io/)
- [Docker Compose](https://docs.docker.com/compose/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

## Quick Reference

### Essential URLs