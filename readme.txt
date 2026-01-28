to connect to postgresql running in docker
From the Terminal (Inside the Container)
If you want to run SQL commands directly inside the running container without an external tool:

Enter the container:

Bash
> docker exec -it restaurant-picker-db bash
Log into Postgres:

Bash
psql -U postgres -d your_db_name

>psql -U postgres -d restaurant_picker

> restaurant_picker=# \dt
                    List of relations
 Schema |             Name             | Type  |  Owner
--------+------------------------------+-------+----------
 public | batch_job_execution          | table | postgres
 public | batch_job_execution_context  | table | postgres
 public | batch_job_execution_params   | table | postgres
 public | batch_job_instance           | table | postgres
 public | batch_step_execution         | table | postgres
 public | batch_step_execution_context | table | postgres
 public | restaurant_submissions       | table | postgres
 public | session_participants         | table | postgres
 public | sessions                     | table | postgres
 public | users                        | table | postgres
(10 rows)

> select * from users;


## **Step 8: Running the Application**

### **Option 1: Running with Docker Compose (Recommended)**
```bash
# From root directory
docker-compose up --build
```

### **Option 2: Running Manually**

1. **Start PostgreSQL**
```bash
docker run -d -p 5432:5432 -e POSTGRES_DB=restaurant_picker -e POSTGRES_PASSWORD=postgres postgres:15
```

2. **Build all services**
```bash
mvn clean install -DskipTests
```

3. **Start services in order**
```bash
# Terminal 1 - User Service
cd user-service
mvn spring-boot:run

# Terminal 2 - Session Service
cd session-service
mvn spring-boot:run

# Terminal 3 - API Gateway
cd api-gateway
mvn spring-boot:run
```

---

## **API Documentation**

### **User Service APIs**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{userId}` | Get user by ID |
| GET | `/api/users/{userId}/can-initiate` | Check if user can initiate session |

### **Session Service APIs**

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| POST | `/api/sessions` | Create new session | `{"userId": "string"}` |
| POST | `/api/sessions/{sessionId}/join` | Join session | `{"userId": "string"}` |
| POST | `/api/sessions/{sessionId}/submit` | Submit restaurant | `RestaurantSubmissionDTO` |
| POST | `/api/sessions/{sessionId}/end` | End session | `{"userId": "string"}` |
| GET | `/api/sessions/{sessionId}` | Get session details | - |
| GET | `/api/sessions` | Get all active sessions | - |
| GET | `/api/sessions/{sessionId}/submissions` | Get all submissions | - |

---

## **Example API Usage**

### 1. Create Session
```bash
curl -X POST http://localhost:8080/api/sessions \
  -H "Content-Type: application/json" \
  -d '{"userId": "<user-id-from-database>"}'
```

### 2. Join Session
```bash
curl -X POST http://localhost:8080/api/sessions/{sessionId}/join \
  -H "Content-Type: application/json" \
  -d '{"userId": "<another-user-id>"}'
```

### 3. Submit Restaurant
```bash
curl -X POST http://localhost:8080/api/sessions/{sessionId}/submit \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantName": "Pizza Paradise",
    "address": "123 Main St",
    "description": "Best pizza in town",
    "submittedByUserId": "<user-id>"
  }'
```

### 4. End Session
```bash
curl -X POST http://localhost:8080/api/sessions/{sessionId}/end \
  -H "Content-Type: application/json" \
  -d '{"userId": "<initiator-user-id>"}'
```

---

## **Key Design Decisions**

### **1. Microservices Architecture**
- **Why**: Scalability, independent deployment, technology flexibility
- **Trade-off**: More complex than monolith, requires service coordination

### **2. Spring Batch for User Loading**
- **Why**: Required by specification, handles large datasets efficiently
- **Benefits**: Chunk processing, transaction management, restart capability
- **Alternative**: Direct database seeding with Liquibase/Flyway

### **3. PostgreSQL Database**
- **Why**: ACID compliance, relational data, JSON support
- **Alternative**: MongoDB for document-based approach, Redis for session state

### **4. WebClient for Inter-Service Communication**
- **Why**: Non-blocking, reactive, better performance than RestTemplate
- **Alternative**: Feign Client (simpler but blocking), gRPC (faster but complex)

### **5. Input Sanitization**
- **Why**: Prevents XSS attacks, ensures display consistency
- **Implementation**: Strip HTML tags, limit character lengths

### **6. API Gateway Pattern**
- **Why**: Single entry point, centralized routing, simplified client
- **Alternative**: Direct service calls, service mesh (Istio)

---

## **Production Considerations**

### **1. Security**
- Add Spring Security with JWT authentication
- Implement rate limiting
- Use HTTPS/TLS

### **2. Observability**
- Add Prometheus + Grafana for metrics
- Implement distributed tracing (Zipkin/Jaeger)
- Centralized logging (ELK stack)

### **3. Resilience**
- Circuit breakers (Resilience4j)
- Retry mechanisms
- Bulkheads for resource isolation

### **4. Scalability**
- Kubernetes for orchestration
- Horizontal pod autoscaling
- Database replication and sharding

### **5. Testing**
- Integration tests with Testcontainers
- Contract testing with Pact
- Load testing with JMeter/Gatling