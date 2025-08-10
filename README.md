# ObserveTask User Service

The User Service is the core authentication and user management microservice for the ObserveTask platform. It provides multi-tenant user management, role-based authorization, JWT token management, and user invitation workflows.

## 🏗️ **Current Implementation Status**

✅ **COMPLETE - Core Foundation (E2.T01)**
- Entity layer with full JPA mappings
- Repository layer with business logic queries
- Database schema with migrations
- Multi-tenant architecture support
- Hybrid authentication preparation (Local + SSO)

🚧 **IN PROGRESS - Next Phases**
- Authentication & Authorization (E2.T02)
- REST Controllers (E2.T03)
- Kafka Event Integration (E2.T04)
- Kubernetes Deployment (E2.T05)

## 🎯 **Features**

### **Multi-Tenant User Management**
- Organization-based user isolation
- Role hierarchy: SUPER_ADMIN → ORG_ADMIN → TEAM_ADMIN → TEAM_MEMBER
- Cross-organization user support with different roles per organization

### **Hybrid Authentication Support**
- **Local Authentication**: Username/password with bcrypt hashing
- **SSO Integration**: Auth0, SAML, Google OAuth, Microsoft OAuth
- **JWT Token Management**: Access tokens with refresh token rotation
- **Session Management**: Multi-device support with token blacklisting

### **User Invitation System**
- Email-based user invitations with expiration
- Role pre-assignment during invitation
- Invitation lifecycle management (PENDING → ACCEPTED/EXPIRED/REVOKED)
- Duplicate invitation prevention

## 🏛️ **Architecture**

### **Entity Relationships**
```
User (1) ←→ (M) UserRole ←→ (1) Organization
User (1) ←→ (M) RefreshToken
User (1) ←→ (M) Invitation (invited user)
```

### **Database Schema**
- **Schema**: `observetask_users`
- **Tables**: users, user_roles, jwt_refresh_tokens, jwt_blacklist, invitations
- **Indexes**: Optimized for query performance and uniqueness constraints
- **Sample Data**: Admin and SSO test users included

## 📊 **Data Model Overview**

### **Core Entities**

**User** - Central user account with hybrid authentication support
- Supports both local (username/password) and SSO authentication
- Stores user profile information and authentication provider details
- Email-based identification with unique constraints

**UserRole** - Multi-tenant role assignments
- Links users to organizations with specific roles
- Enables users to have different permissions per organization
- Supports role hierarchy from SUPER_ADMIN to TEAM_MEMBER

**RefreshToken** - JWT token lifecycle management
- Manages refresh tokens for secure authentication sessions
- Supports multi-device login with device tracking
- Automatic expiration and cleanup capabilities

**Invitation** - User onboarding workflow
- Email-based invitation system with expiration
- Pre-assigns roles during invitation process
- Tracks invitation lifecycle (pending, accepted, expired, revoked)

## 🗄️ **Database Setup**

### **Prerequisites**
- PostgreSQL 13+
- Database: `observetask_db`
- Schema: `observetask_users`

### **Migration**
```bash
# Flyway migration runs automatically on startup
# Location: src/main/resources/db/migration/V1__Create_users_schema.sql
```

### **Sample Data**
```sql
-- Local admin user (password: admin123)
Email: admin@observetask.demo
Role: SUPER_ADMIN

-- SSO test user
Email: sso.user@observetask.demo  
Provider: AUTH0
Role: ORG_ADMIN
```

## ⚙️ **Configuration**

### **application.yml**
```yaml
spring:
  application:
    name: observetask-user-service
  
  # Database Configuration
  datasource:
    url: jdbc:postgresql://host.minikube.internal:5432/observetask_db
    username: observetask_user_svc
    password: user_service_password
  
  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        default_schema: observetask_users
  
  # Kafka Configuration (Event Publishing)
  kafka:
    bootstrap-servers: host.minikube.internal:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
  
  # Redis Configuration (Caching & Token Blacklist)
  data:
    redis:
      host: host.minikube.internal
      port: 6379
      database: 1

# JWT Configuration
jwt:
  secret: ${JWT_SECRET:ObserveTask-Super-Secret-Key-For-Development-Only-2024}
  access-token-expiration: 900000    # 15 minutes
  refresh-token-expiration: 604800000 # 7 days
```

## 🔧 **Development Setup**

### **Prerequisites**
- Java 21+
- Maven 3.8+
- PostgreSQL running on localhost:5432
- Redis running on localhost:6379
- Kafka running on localhost:9092

### **Local Development**
```bash
# Clone repository
git clone https://github.com/observetask/observetask-user-service.git
cd observetask-user-service

# Build project
mvn clean compile

# Run tests
mvn test

# Start application
mvn spring-boot:run
```

### **Docker Development**
```bash
# Build Docker image
docker build -t observetask/user-service:latest .

# Run with docker-compose (database dependencies)
docker-compose up -d
```

## 📚 **Repository Layer**

### **UserRepository**
```java
// User management operations
Optional<User> findByEmail(String email);
List<User> findByOrganizationId(UUID organizationId);
Optional<User> findByExternalIdAndAuthProvider(String externalId, AuthProvider provider);
```

### **UserRoleRepository**
```java
// Multi-tenant role management
List<UserRole> findByUserId(UUID userId);
Optional<UserRole> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);
List<UserRole> findByOrganizationIdAndRole(UUID organizationId, Role role);
```

### **RefreshTokenRepository**
```java
// JWT token lifecycle
Optional<RefreshToken> findByTokenHash(String tokenHash);
List<RefreshToken> findByUserId(UUID userId);
Boolean existsValidToken(String tokenHash); // Includes expiration check
Long countActiveTokensByUser(UUID userId);
```

### **InvitationRepository**
```java
// Invitation workflow
Optional<Invitation> findByToken(String token);
List<Invitation> findByEmail(String email);
Boolean existsPendingInvitationByEmailAndOrganization(String email, UUID orgId);
List<Invitation> findInvitationsExpiringSoon(LocalDateTime alertTime);
```

## 🚀 **Testing**

### **Repository Tests**
```bash
# Run repository integration tests
mvn test -Dtest=*RepositoryTest
```

### **Database Connectivity**
```bash
# Test database connection
psql -h localhost -U observetask_user_svc -d observetask_db -c "SELECT COUNT(*) FROM observetask_users.users;"
```

## 📋 **API Documentation (Coming Soon)**

🚧 **Planned REST Endpoints**:
- `POST /auth/login` - User authentication
- `POST /auth/refresh` - Token refresh
- `POST /auth/logout` - User logout
- `GET /users/profile` - Get user profile
- `PUT /users/profile` - Update user profile
- `POST /users/invite` - Send user invitation

## 🔄 **Event Publishing (Coming Soon)**

🚧 **Planned Kafka Events**:
- `user.created` - User registration
- `user.invited` - User invitation sent
- `user.role.updated` - Role changes
- `user.login` - Authentication events

## 🐳 **Deployment**

### **Kubernetes**
```bash
# Deploy to Kubernetes
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -l app=observetask-user-service
```

### **Health Checks**
- **Readiness**: `GET /actuator/health/readiness`
- **Liveness**: `GET /actuator/health/liveness`
- **Metrics**: `GET /actuator/prometheus`

## 🔒 **Security Features**

### **Password Security**
- bcrypt hashing with 12 salt rounds
- Password validation rules (coming soon)

### **JWT Security**
- RS256 signing algorithm
- 15-minute access token expiration
- 7-day refresh token expiration
- Token blacklisting with Redis

### **Multi-Tenant Security**
- Organization-based data isolation
- Role-based authorization
- Cross-organization permission prevention

## 🧪 **Testing Strategy**

### **Unit Tests**
- Entity validation tests
- Repository method tests
- Business logic tests

### **Integration Tests**
- Database integration with TestContainers
- Repository layer integration tests
- Security configuration tests

## 📈 **Monitoring & Observability**

### **Metrics**
- Spring Boot Actuator endpoints
- Prometheus metrics exposure
- Custom business metrics (coming soon)

### **Logging**
- Structured JSON logging
- Request/response logging
- Security event logging

## 🔮 **Roadmap**

### **Phase 1: Authentication (E2.T02)**
- JWT token generation and validation
- Password hashing and verification
- OAuth2/SSO integration preparation

### **Phase 2: REST APIs (E2.T03)**
- Authentication endpoints
- User profile management
- User invitation APIs

### **Phase 3: Event Integration (E2.T04)**
- Kafka event publishing
- Cross-service communication
- Event sourcing setup

### **Phase 4: Deployment (E2.T05)**
- Production Kubernetes deployment
- Environment-specific configurations
- Health monitoring setup

## 🤝 **Contributing**

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 **License**

This project is part of the ObserveTask demonstration platform.

---

**Built with:** Spring Boot 3.2, Java 21, PostgreSQL, Redis, Kafka  
**Part of:** ObserveTask Microservices Platform  
**Status:** Core Foundation Complete ✅