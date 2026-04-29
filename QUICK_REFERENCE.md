# ⚡ Quick Reference Card - DevOps CI/CD Pipeline

## 🚀 FASTEST START (5 minutes)

```bash
# 1. Start infrastructure
docker-compose up -d

# 2. Wait for services (check with docker ps)
docker ps

# 3. Access Jenkins
# http://localhost:8888
# Get password: docker logs jenkins | grep "Initial Admin password"

# 4. Get SonarQube admin token
# Login to http://localhost:9000 (admin/admin)
# Go to Security → Users → Tokens
# Generate "jenkins-token"
```

---

## 📋 JENKINS JOBS TO CREATE

### CI Pipelines (Continuous Integration)

| Job Name | Repository | Jenkinsfile | Branch |
|---|---|---|---|
| **article-service-CI** | FormeBack | `article-service/Jenkinsfile` | main |
| **document-service-CI** | FormeBack | `document-service/Jenkinsfile` | main |
| **shop-service-CI** | FormeBack | `gestion shop/Jenkinsfile` | main |

### CD Pipelines (Continuous Deployment)

| Job Name | Repository | Jenkinsfile | Branch |
|---|---|---|---|
| **article-service-CD** | FormeBack | `article-service/Jenkinsfile_CD` | main |
| **document-service-CD** | FormeBack | `document-service/Jenkinsfile_CD` | main |
| **shop-service-CD** | FormeBack | `gestion shop/Jenkinsfile_CD` | main |

---

## 🔑 JENKINS CREDENTIALS NEEDED

### Create These Credentials in Jenkins

**Manage Jenkins → Credentials → System → Global credentials → Add Credentials**

1. **DOCKERHUB_CREDENTIALS**
   - Kind: Username with password
   - Username: `your-dockerhub-username`
   - Password: `your-dockerhub-token`
   - ID: `DOCKERHUB_CREDENTIALS`

2. **SONAR_TOKEN**
   - Kind: Secret text
   - Secret: `your-sonarqube-token`
   - ID: `SONAR_TOKEN`

---

## 🔗 IMPORTANT URLS

### Access Points

```
Jenkins:      http://localhost:8888
SonarQube:    http://localhost:9000
Prometheus:   http://localhost:9090
Grafana:      http://localhost:3000
Eureka:       http://localhost:8761

API Gateway:  http://localhost:8080
MySQL:        localhost:3306
```

### Microservice Prometheus Endpoints

```
Article:      http://localhost:8086/actuator/prometheus
Document:     http://localhost:8085/actuator/prometheus
Shop:         http://localhost:8083/actuator/prometheus
User:         http://localhost:8081/actuator/prometheus
API Gateway:  http://localhost:8082/actuator/prometheus
Eureka:       http://localhost:8761/actuator/prometheus
```

---

## 🔐 DEFAULT CREDENTIALS

| Service | User | Password |
|---|---|---|
| SonarQube | admin | admin |
| Grafana | admin | admin |
| MySQL | root | (empty) |
| Jenkins | (created during setup) | (created during setup) |

---

## 📊 MICROSERVICE DETAILS

### Article Service
- **Port:** 8086
- **Context:** /api
- **Database:** MySQL - forme
- **JAR:** article-service-0.0.1-SNAPSHOT.jar
- **Health:** GET /actuator/health

### Document Service
- **Port:** 8085
- **Context:** /api
- **Database:** MySQL - forme
- **JAR:** document-service-0.0.1-SNAPSHOT.jar
- **Health:** GET /actuator/health

### Shop Service
- **Port:** 8083
- **Context:** /gestionshop
- **Database:** MySQL - forme
- **JAR:** shop-service-0.0.1-SNAPSHOT.jar
- **Health:** GET /actuator/health

---

## 🛠️ COMMON COMMANDS

### Docker Management

```bash
# View all containers
docker ps -a

# View logs
docker logs -f container-name

# Restart service
docker restart article-service

# Stop all
docker-compose down

# Start all
docker-compose up -d

# Rebuild and start
docker-compose up -d --build
```

### Maven Build

```bash
# Build single service
cd article-service
mvn clean package -DskipTests

# Build all
mvn clean package -DskipTests -T 1C

# Run tests
mvn test

# SonarQube analysis
mvn sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=TOKEN
```

### Health Checks

```bash
# Test service health
curl http://localhost:8086/actuator/health

# Get metrics
curl http://localhost:8086/actuator/prometheus | head

# Test Eureka
curl http://localhost:8761/eureka/apps/article-service

# Test API Gateway
curl http://localhost:8080/user-service/api/users
```

---

## 🔄 PIPELINE EXECUTION FLOW

### CI Pipeline (Jenkinsfile)

```
1. Git Checkout ───────► Clone from GitHub
2. Unit Tests ─────────► mvn test
3. SonarQube ──────────► mvn sonar:sonar
4. Build JAR ──────────► mvn package -DskipTests
5. Docker Build ───────► docker build -t image:latest .
6. Docker Push ────────► docker push to DockerHub
```

**Artifacts:** Docker image on DockerHub

---

### CD Pipeline (Jenkinsfile_CD)

```
1. Deploy ─────────────► Stop old container → Pull image → Run new container
2. Health Check ───────► Poll /actuator/health (max 30 attempts, 10s each)
3. Success ────────────► HTTP 200 received
```

**Verification:** Service responds on expected port

---

## 📈 PROMETHEUS QUERIES

### Common Metrics

```promql
# JVM Memory
jvm_memory_used_bytes

# HTTP Requests
rate(http_requests_total[5m])

# Request Duration
histogram_quantile(0.95, http_request_duration_seconds)

# Uptime
process_uptime_seconds

# CPU Usage
process_cpu_usage

# Errors Rate
rate(http_requests_total{status=~"5.."}[5m])
```

---

## 📝 TROUBLESHOOTING QUICK FIXES

### Jenkins won't start

```bash
docker logs jenkins
docker restart jenkins
```

### Can't push to DockerHub

```bash
docker login
# Verify credentials in Jenkins
```

### SonarQube down

```bash
docker logs sonarqube
docker restart sonarqube sonarqube-postgres
```

### Microservice not registering

```bash
# Check environment variables
docker exec article-service env | grep EUREKA
# Verify Eureka is running
docker logs eureka-server
```

### Prometheus not collecting metrics

```bash
# Test endpoint
curl http://localhost:8086/actuator/prometheus

# Check prometheus.yml config
cat prometheus.yml
```

---

## 🎯 VARIABLES TO REPLACE

In all Jenkinsfiles and configs, replace these:

| Variable | Replace With |
|---|---|
| `dockerhub_username` | Your DockerHub username |
| `SONAR_TOKEN` | Your SonarQube token |
| `DOCKERHUB_CREDENTIALS` | Jenkins credential ID |

**In:** Jenkinsfile, Jenkinsfile_CD, docker-compose.yml, .env

---

## ✅ SETUP CHECKLIST

- [ ] Docker Desktop running
- [ ] Repository cloned
- [ ] docker-compose up -d executed
- [ ] All containers started (docker ps shows 10+)
- [ ] Jenkins accessible (localhost:8888)
- [ ] Jenkins initial setup complete
- [ ] DockerHub credentials added to Jenkins
- [ ] SonarQube token created and added to Jenkins
- [ ] CI job for article-service created
- [ ] First build executed successfully
- [ ] Docker image found on DockerHub
- [ ] SonarQube project visible
- [ ] CD job created
- [ ] Prometheus metrics visible
- [ ] Grafana dashboard created

---

## 🚀 DEPLOY NEW VERSION

```bash
# 1. Make code changes
# 2. Commit and push
git add .
git commit -m "Feature: xyz"
git push origin main

# 3. Jenkins CI pipeline triggered automatically (or manually)
# 4. Check SonarQube for quality issues
# 5. Approve to deploy

# 6. Trigger CD pipeline (manual or automatic)
# 7. Monitor deployment

# 8. Verify metrics in Prometheus/Grafana
```

---

## 📞 EMERGENCY COMMANDS

### Reset Everything

```bash
docker-compose down -v
docker-compose up -d --build
```

### Force Rebuild

```bash
cd article-service
mvn clean package -DskipTests -U
docker build -t dockerhub_username/article-service:latest .
docker push dockerhub_username/article-service:latest
```

### View All Logs

```bash
docker-compose logs --tail=100 -f
```

### Container Status

```bash
docker inspect article-service | grep Status
```

---

## 📚 FILES LOCATION

```
FormeBack/
├── docker-compose.yml           ← Main orchestration
├── prometheus.yml                ← Metrics config
├── DEVOPS_SETUP_GUIDE.md         ← Full documentation
├── IMPLEMENTATION_SUMMARY.md     ← This file reference
│
├── article-service/
│   ├── Dockerfile
│   ├── Jenkinsfile (CI)
│   ├── Jenkinsfile_CD
│   ├── pom.xml
│   └── src/main/resources/application.properties
│
├── document-service/
│   ├── Dockerfile
│   ├── Jenkinsfile (CI)
│   ├── Jenkinsfile_CD
│   ├── pom.xml
│   └── src/main/resources/application.properties
│
└── gestion shop/
    ├── Dockerfile
    ├── Jenkinsfile (CI)
    ├── Jenkinsfile_CD
    ├── pom.xml
    └── src/main/resources/application.yml
```

---

## 🎓 NEXT STEPS

1. ✅ Complete setup (see checklist above)
2. ✅ Run first CI pipeline
3. ✅ Create Grafana dashboards
4. ✅ Set up Prometheus alerting rules
5. ✅ Configure backup strategy
6. ✅ Document custom metrics
7. ✅ Plan production deployment
8. ✅ Set up disaster recovery

---

**Version:** 1.0  
**Last Updated:** April 28, 2026  
**Status:** Production Ready ✅

**Quick Help:** See DEVOPS_SETUP_GUIDE.md for detailed instructions
