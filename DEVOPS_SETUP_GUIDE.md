# 🚀 ForMe DevOps CI/CD Infrastructure Setup Guide

## 📋 Overview

This guide provides complete instructions for setting up the CI/CD DevOps infrastructure for the ForMe microservices platform.

**Infrastructure Components:**
- **Jenkins** - CI/CD orchestration
- **SonarQube** - Code quality analysis
- **Prometheus** - Metrics collection
- **Grafana** - Visualization and dashboards
- **Docker** - Containerization
- **DockerHub** - Image registry

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                        Development Workflow                       │
└──────────────────────────────────────────────────────────────────┘
                                │
                    ┌───────────┼───────────┐
                    ▼           ▼           ▼
            ┌──────────────────────────────────────┐
            │   Git Push to GitHub Repository       │
            │   ├─ article-service                  │
            │   ├─ document-service                 │
            │   └─ shop-service                     │
            └──────────────────────────────────────┘
                                │
                                ▼
                    ┌──────────────────────┐
                    │   Jenkins Webhook    │
                    │   Triggers Pipeline  │
                    └──────────────────────┘
                                │
                    ┌───────────┼───────────┐
                    ▼           ▼           ▼
            ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
            │CI Pipeline   │ │CI Pipeline   │ │CI Pipeline   │
            │article-svce  │ │document-svce │ │shop-service  │
            └──────────────┘ └──────────────┘ └──────────────┘
                    │           │                   │
        ┌───────────┴───────────┴───────────────────┘
        │
        ├─► Git Checkout
        ├─► Unit Tests
        ├─► SonarQube Analysis
        ├─► Build JAR
        ├─► Docker Build & Push
        │
        └─► CD Pipeline (Manual/Webhook)
            ├─► Pull Docker Image
            ├─► Deploy Container
            ├─► Health Check
            └─► Status Report

            ▼
    ┌─────────────────────────────────────┐
    │   Monitoring & Observability        │
    │   ├─ Prometheus Metrics Scraping    │
    │   ├─ Grafana Dashboards             │
    │   ├─ Actuator Endpoints             │
    │   └─ Real-time Alerts               │
    └─────────────────────────────────────┘
```

---

## 🔧 Prerequisites

1. **Docker Desktop** (latest version)
   - Download: https://www.docker.com/products/docker-desktop/

2. **Docker Hub Account**
   - Sign up: https://hub.docker.com/
   - Create personal access token for Jenkins

3. **Git** & **GitHub Account**
   - Forked repositories ready
   - SSH keys configured (optional)

4. **System Requirements**
   - Minimum 8GB RAM
   - 20GB free disk space
   - Ports available: 8080-8093, 3000, 9000, 9090

---

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/samiwin1/FormeBack.git
cd FormeBack
```

### 2. Start Docker Compose Stack

```bash
# Build all images
docker-compose build

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f
```

### 3. Verify Services Are Running

```bash
# Check all containers
docker ps

# Expected output should show:
# - forme-mysql
# - eureka-server
# - api-gateway
# - user-service
# - document-service
# - article-service
# - shop-service
# - sonarqube
# - jenkins
# - prometheus
# - grafana
```

### 4. Access Services

| Service | URL | Credentials |
|---------|-----|-------------|
| **Jenkins** | http://localhost:8888 | Admin credentials (see step 5) |
| **SonarQube** | http://localhost:9000 | admin / admin |
| **Prometheus** | http://localhost:9090 | None required |
| **Grafana** | http://localhost:3000 | admin / admin |
| **API Gateway** | http://localhost:8080 | N/A |
| **Eureka** | http://localhost:8761 | None required |

---

## 🔐 Jenkins Setup

### Get Jenkins Initial Password

```bash
docker logs jenkins | grep "Initial Admin password"
```

### First-Time Configuration

1. **Unlock Jenkins**
   - Go to http://localhost:8888
   - Paste the initial admin password
   - Click "Continue"

2. **Install Suggested Plugins**
   - Click "Install suggested plugins"
   - Wait for installation (5-10 minutes)

3. **Create Admin User**
   - Fill in username, password, email
   - Click "Save and Continue"

4. **Configure Jenkins URL**
   - Set to: `http://localhost:8888`
   - Click "Save and Finish"

---

## 🔑 Configure Jenkins Credentials

### Add DockerHub Credentials

1. Go to **Jenkins Dashboard** → **Manage Jenkins** → **Credentials**
2. Click **System** → **Global credentials**
3. Click **Add Credentials**
4. Fill in:
   - **Kind:** Username with password
   - **Scope:** Global
   - **Username:** `your-dockerhub-username`
   - **Password:** `your-dockerhub-token`
   - **ID:** `DOCKERHUB_CREDENTIALS`
   - Click **Create**

### Add SonarQube Token

1. Go to **SonarQube** (http://localhost:9000)
2. Login with `admin / admin`
3. Click **Security** → **Users** → **Tokens**
4. Generate new token named `jenkins-token`
5. Copy the token

Back to Jenkins:
1. **Manage Jenkins** → **Credentials** → **Add Credentials**
2. Fill in:
   - **Kind:** Secret text
   - **Secret:** `paste-sonarqube-token`
   - **ID:** `SONAR_TOKEN`
   - Click **Create**

---

## 📝 Create Jenkins Pipelines

### For Each Microservice (article, document, shop):

1. Go to **Jenkins Dashboard**
2. Click **New Item**
3. Enter Job Name: `article-service-CI` (repeat for each service)
4. Select **Pipeline**
5. Click **OK**
6. **Pipeline** section:
   - Select **Pipeline script from SCM**
   - **SCM:** Git
   - **Repository URL:** `https://github.com/samiwin1/FormeBack.git`
   - **Branch:** `*/main`
   - **Script Path:** `article-service/Jenkinsfile` (adjust per service)
7. Click **Save**

### Create CD Pipeline Jobs

1. Click **New Item**
2. Enter Job Name: `article-service-CD`
3. Select **Pipeline**
4. **Pipeline** section:
   - Select **Pipeline script from SCM**
   - **SCM:** Git
   - **Repository URL:** `https://github.com/samiwin1/FormeBack.git`
   - **Branch:** `*/main`
   - **Script Path:** `article-service/Jenkinsfile_CD`
5. Click **Save**

---

## 📊 SonarQube Configuration

### Initial Setup

1. Go to http://localhost:9000
2. Login: `admin / admin`
3. Change default password when prompted

### Create Project

1. Click **Projects** → **Create Project**
2. Fill in:
   - **Key:** `article-service`
   - **Name:** `Article Service`
   - **Main Branch:** `main`
3. Click **Create Project**
4. Select **Use existing token** or create new
5. Choose **Maven** as build tool
6. Copy the provided command

Repeat for each microservice:
- `document-service`
- `shop-service`

### Access Quality Reports

After pipeline runs, view reports at:
```
http://localhost:9000/dashboard?id=article-service
```

---

## 📈 Prometheus Configuration

### Already Configured

The `prometheus.yml` file is pre-configured to scrape metrics from:
- Article Service (port 8086)
- Document Service (port 8085)
- Shop Service (port 8083)
- User Service (port 8081)
- API Gateway (port 8082)
- Eureka Server (port 8761)

### Access Metrics

Visit: http://localhost:9090

**Query Examples:**
```promql
# JVM Memory Usage
jvm_memory_used_bytes

# HTTP Request Rate
rate(http_requests_total[5m])

# Application Uptime
process_uptime_seconds
```

---

## 📊 Grafana Configuration

### Login

1. Go to http://localhost:3000
2. Login: `admin / admin`
3. Change password (recommended)

### Add Prometheus Datasource

1. **Settings** → **Data Sources** → **Add new data source**
2. **Type:** Prometheus
3. **URL:** `http://prometheus:9090`
4. Click **Save & Test**

### Create Dashboard

1. **Dashboards** → **Create** → **New Dashboard**
2. **Add Panel**
3. **Datasource:** Prometheus
4. **Metrics:** Select from available metrics
5. **Visualization:** Choose appropriate chart type
6. **Save Panel**

### Pre-built Dashboard Templates

Import community dashboards:
1. **Dashboards** → **Import**
2. Dashboard ID: `1860` (Node Exporter for Prometheus)
3. Select **Prometheus** datasource
4. Click **Import**

---

## 🔄 CI/CD Pipeline Workflow

### 1. CI Pipeline (Jenkinsfile)

**Stages:**

```
Git Checkout
    ↓
Unit Tests (mvn test)
    ↓
SonarQube Analysis (mvn sonar:sonar)
    ↓
Build JAR (mvn package -DskipTests)
    ↓
Docker Build & Push
    ↓
SUCCESS
```

**Triggered by:**
- Git push to `main` branch
- Manual Jenkins job trigger
- Webhook from GitHub (optional)

### 2. CD Pipeline (Jenkinsfile_CD)

**Stages:**

```
Deploy (docker run)
    ↓
Monitoring Check (curl /actuator/health)
    ↓
Health Verification (HTTP 200)
    ↓
SUCCESS
```

**Triggered by:**
- Manual trigger
- Scheduled job
- Completion of CI pipeline

---

## 🐛 Troubleshooting

### Jenkins Pipeline Fails

```bash
# Check Jenkins logs
docker logs jenkins

# Restart Jenkins
docker restart jenkins
```

### SonarQube Not Accessible

```bash
# Check SonarQube logs
docker logs sonarqube

# Ensure PostgreSQL is running
docker logs sonarqube-postgres
```

### Docker Login Fails in Pipeline

1. Verify DockerHub credentials in Jenkins
2. Check if Docker daemon is accessible
3. Test locally:
   ```bash
   docker login -u your-username -p your-token
   ```

### Services Not Registering with Eureka

1. Check Eureka logs:
   ```bash
   docker logs eureka-server
   ```
2. Verify environment variables in docker-compose.yml
3. Ensure network connectivity between containers

### Prometheus Not Scraping Metrics

1. Verify `/actuator/prometheus` endpoint:
   ```bash
   curl http://localhost:8086/actuator/prometheus
   ```
2. Check prometheus.yml configuration
3. Verify service ports match docker-compose.yml

---

## 📦 Environment Variables

Create a `.env` file in project root:

```bash
# DockerHub
DOCKERHUB_USERNAME=your-username
DOCKERHUB_TOKEN=your-token

# SonarQube
SONAR_HOST_URL=http://sonarqube:9000
SONAR_LOGIN=your-sonarqube-token

# Database
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=forme

# Microservices
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

# Stripe (for shop-service)
STRIPE_SECRET_KEY=your-stripe-key

# Optional: LibreTranslate
LIBRE_TRANSLATE_BASE_URL=http://localhost:5000
LIBRE_TRANSLATE_API_KEY=optional-key
```

---

## 📝 Updating Dockerfiles

### Standard Format (All Services)

```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/SERVICE-NAME-0.0.1-SNAPSHOT.jar app.jar

EXPOSE PORT

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 🔄 Building Microservices Manually

```bash
# Build single service
cd article-service
mvn clean package -DskipTests

# Build all services
cd ..
mvn clean package -DskipTests

# Build specific modules
mvn clean package -DskipTests -pl article-service,document-service

# Skip tests for faster builds
mvn clean package -DskipTests -T 1C
```

---

## 🚢 Deploy to Production

### Prerequisites

1. Linux server with Docker
2. Domain name and SSL certificate
3. Kubernetes cluster (optional, for scaling)

### Manual Deployment

```bash
# Pull latest images
docker pull dockerhub_username/article-service:latest
docker pull dockerhub_username/document-service:latest
docker pull dockerhub_username/shop-service:latest

# Stop old containers
docker stop article-service document-service shop-service

# Remove old containers
docker rm article-service document-service shop-service

# Start new containers
docker-compose -f docker-compose.prod.yml up -d
```

### Kubernetes Deployment (Optional)

Create `k8s/` directory with:
- `deployment.yaml`
- `service.yaml`
- `configmap.yaml`
- `secret.yaml`

Deploy with:
```bash
kubectl apply -f k8s/
```

---

## 📚 Resources

- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [SonarQube Documentation](https://docs.sonarqube.org/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)

---

## 📞 Support

For issues or questions:

1. Check the **Troubleshooting** section
2. Review Jenkins/SonarQube logs
3. Open GitHub issue
4. Contact DevOps team

---

## ✅ Checklist

- [ ] Docker Desktop installed and running
- [ ] Repository cloned
- [ ] Docker Compose stack started
- [ ] All services running (docker ps)
- [ ] Jenkins initial setup complete
- [ ] DockerHub credentials added
- [ ] SonarQube token created
- [ ] CI/CD pipelines configured
- [ ] First build successful
- [ ] Prometheus metrics visible
- [ ] Grafana dashboard created
- [ ] Production deployment plan ready

---

**Last Updated:** April 28, 2026  
**Version:** 1.0  
**Status:** Production Ready ✅
