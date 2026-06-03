# Spring Boot Pulsar Order Processing System

A cloud-native backend application demonstrating event-driven architecture using Java Spring Boot, Apache Pulsar, PostgreSQL, Docker, Kubernetes, and GitHub Actions.

## Overview

This project is an end-to-end production-ready backend application that exposes REST APIs for creating and retrieving customer orders. It implements a decoupled microservices pattern where:

1. **REST API Layer**: Handles order creation and retrieval requests
2. **Database Layer**: Persists orders in PostgreSQL with status tracking
3. **Event-Driven Layer**: Publishes order events to Apache Pulsar for asynchronous processing
4. **Consumer Layer**: Listens to Pulsar topics, processes events, and updates order status

## Architecture

```
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│ REST Client │────────►│ Spring Boot  │────────►│ PostgreSQL   │
└─────────────┘         │ Application  │         └──────────────┘
                        └──────┬───────┘
                               │
                               ▼
                        ┌──────────────┐
                        │ Apache Pulsar│
                        │    Topic     │
                        └──────┬───────┘
                               │
                               ▼
                        ┌──────────────┐
                        │   Pulsar     │
                        │   Consumer   │
                        └──────┬───────┘
                               │
                               ▼
                        ┌──────────────┐
                        │ PostgreSQL   │
                        │ (Status Upd.)│
                        └──────────────┘
```

## Key Features

- **Event-Driven Architecture**: Decoupled order processing using pub/sub messaging
- **Asynchronous Processing**: Non-blocking order status updates
- **Cloud-Native Design**: Containerized with Docker and orchestrated with Kubernetes
- **CI/CD Pipeline**: Automated testing and deployment via GitHub Actions
- **Scalable Infrastructure**: Optional Argo CD for GitOps-based deployment
- **RESTful APIs**: Clean and intuitive endpoint design
- **Database Persistence**: Reliable order storage with PostgreSQL

## Tech Stack

- **Language**: Java
- **Framework**: Spring Boot
- **Message Broker**: Apache Pulsar
- **Database**: PostgreSQL
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **CI/CD**: GitHub Actions
- **GitOps** (Optional): Argo CD

## Project Structure

```
Project-1/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/order/
│   │   │       ├── controller/          # REST API endpoints
│   │   │       ├── service/             # Business logic
│   │   │       ├── repository/          # Data access layer
│   │   │       ├── model/               # Entity classes
│   │   │       ├── event/               # Pulsar event definitions
│   │   │       └── consumer/            # Pulsar message consumer
│   │   └── resources/
│   │       └── application.properties   # Configuration
│   └── test/                            # Unit and integration tests
├── docker/
│   ├── Dockerfile                       # Application container
│   └── docker-compose.yml               # Local development setup
├── k8s/
│   ├── deployment.yaml                  # Kubernetes deployment
│   ├── service.yaml                     # Kubernetes service
│   ├── configmap.yaml                   # Configuration management
│   └── pulsar-setup.yaml                # Pulsar setup
├── .github/
│   └── workflows/
│       └── ci-cd.yml                    # GitHub Actions workflow
├── README.md                            # This file
└── pom.xml                              # Maven configuration

```

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL 12+ (or use Docker)
- Apache Pulsar (or use Docker Compose)
- kubectl (for Kubernetes deployment)

### Local Development with Docker Compose

1. **Clone the repository**
   ```bash
   git clone https://github.com/sagarikamahalik93/Project-1.git
   cd Project-1
   ```

2. **Start services with Docker Compose**
   ```bash
   docker-compose -f docker/docker-compose.yml up -d
   ```
   This will start:
   - Spring Boot application (port 8080)
   - PostgreSQL (port 5432)
   - Apache Pulsar (port 6650)

3. **Verify services are running**
   ```bash
   docker-compose ps
   ```

4. **Access the application**
   ```
   http://localhost:8080
   ```

### Manual Setup

1. **Build the project**
   ```bash
   mvn clean package
   ```

2. **Start PostgreSQL**
   ```bash
   # Using Docker
   docker run -d \
     -e POSTGRES_USER=order_user \
     -e POSTGRES_PASSWORD=order_password \
     -e POSTGRES_DB=orders_db \
     -p 5432:5432 \
     postgres:15
   ```

3. **Start Apache Pulsar**
   ```bash
   # Using Docker
   docker run -d \
     -p 6650:6650 \
     -p 8080:8080 \
     apachepulsar/pulsar:latest \
     bin/pulsar standalone
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

## API Endpoints

### Create Order

**POST** `/api/orders`

Request body:
```json
{
  "customerId": "CUST123",
  "customerName": "John Doe",
  "items": [
    {
      "productId": "PROD001",
      "quantity": 2,
      "price": 29.99
    }
  ],
  "totalAmount": 59.98
}
```

Response:
```json
{
  "orderId": "ORD20240101001",
  "customerId": "CUST123",
  "status": "CREATED",
  "totalAmount": 59.98,
  "createdAt": "2024-01-01T10:30:00Z"
}
```

### Get Order

**GET** `/api/orders/{orderId}`

Response:
```json
{
  "orderId": "ORD20240101001",
  "customerId": "CUST123",
  "status": "PROCESSED",
  "totalAmount": 59.98,
  "createdAt": "2024-01-01T10:30:00Z",
  "updatedAt": "2024-01-01T10:35:00Z"
}
```

### List Orders

**GET** `/api/orders`

Response:
```json
[
  {
    "orderId": "ORD20240101001",
    "customerId": "CUST123",
    "status": "PROCESSED",
    "totalAmount": 59.98
  }
]
```

## Order Processing Flow

1. **Order Creation**: Client sends POST request to create an order
2. **Database Storage**: Order is stored in PostgreSQL with `CREATED` status
3. **Event Publishing**: Order event is published to Pulsar topic `orders-topic`
4. **Event Consumption**: Pulsar consumer receives the event asynchronously
5. **Processing**: Consumer processes the order (validation, payment, etc.)
6. **Status Update**: Order status is updated to `PROCESSED` in PostgreSQL
7. **Retrieval**: Client can retrieve updated order via GET endpoint

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/orders_db
spring.datasource.username=order_user
spring.datasource.password=order_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL10Dialect

# Pulsar Configuration
pulsar.brokerServiceUrl=pulsar://localhost:6650
pulsar.webServiceUrl=http://localhost:8080
pulsar.orderTopic=orders-topic
pulsar.consumerSubscription=order-consumer

# Logging
logging.level.root=INFO
logging.level.com.order=DEBUG
```

## Docker Deployment

### Build Docker Image

```bash
docker build -f docker/Dockerfile -t order-processor:latest .
```

### Run Container

```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/orders_db \
  -e SPRING_DATASOURCE_USERNAME=order_user \
  -e SPRING_DATASOURCE_PASSWORD=order_password \
  -e PULSAR_BROKERSERVICEURL=pulsar://pulsar:6650 \
  --name order-processor \
  order-processor:latest
```

## Kubernetes Deployment

### Prerequisites

- Kubernetes cluster running
- kubectl configured
- Docker image pushed to registry

### Deploy to Kubernetes

1. **Update image references** in `k8s/deployment.yaml`

2. **Apply manifests**
   ```bash
   kubectl apply -f k8s/configmap.yaml
   kubectl apply -f k8s/pulsar-setup.yaml
   kubectl apply -f k8s/deployment.yaml
   kubectl apply -f k8s/service.yaml
   ```

3. **Verify deployment**
   ```bash
   kubectl get deployments
   kubectl get pods
   kubectl get services
   ```

4. **Access the service**
   ```bash
   kubectl port-forward service/order-processor 8080:8080
   ```

## CI/CD Pipeline

GitHub Actions workflow (`.github/workflows/ci-cd.yml`) includes:

- **Build**: Compile and package application
- **Test**: Run unit and integration tests
- **SonarQube**: Code quality analysis
- **Docker**: Build and push image
- **Deploy**: Deploy to Kubernetes (optional with Argo CD)

## Testing

### Run Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=OrderServiceTest

# Integration tests only
mvn verify -Dgroups=integration
```

## Monitoring & Logging

- **Logs**: Check application logs in `logs/` directory
- **Pulsar Dashboard**: http://localhost:8080 (if running standalone)
- **Database**: Connect to PostgreSQL to inspect orders table

## Troubleshooting

### Connection Issues

- Verify all services are running: `docker ps` or `kubectl get pods`
- Check network connectivity between services
- Review application logs for errors

### Consumer Not Processing Events

- Verify Pulsar topic exists
- Check consumer subscription configuration
- Ensure consumer pod is running and healthy

### Database Migration Errors

- Drop and recreate database
- Check Hibernate DDL settings in `application.properties`
- Review migration logs

## Performance Optimization

- Use connection pooling (HikariCP)
- Enable caching for frequently accessed orders
- Batch process events when possible
- Monitor Pulsar consumer lag

## Security Considerations

- Enable PostgreSQL authentication
- Use Pulsar authentication/authorization
- Implement API rate limiting
- Add input validation on all endpoints
- Use HTTPS in production
- Manage secrets via Kubernetes Secrets or environment variables

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support & Contact

For questions or issues, please open a GitHub issue in the repository.

## Roadmap

- [ ] Add authentication/authorization with Spring Security
- [ ] Implement distributed tracing with Jaeger
- [ ] Add metrics collection with Prometheus
- [ ] Implement API versioning
- [ ] Add GraphQL support
- [ ] Enhanced error handling and validation
- [ ] Performance load testing
- [ ] Documentation with Swagger/OpenAPI

---

**Last Updated**: June 3, 2026
