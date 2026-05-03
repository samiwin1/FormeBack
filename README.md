# FormeBack1 — DevOps Sprint 3

Microservices backend with full CI/CD, Docker, and Kubernetes setup.

## Services

| Service | Port | Path |
|---|---|---|
| mentor-service | 8088 | `FormeBack1/` |
| certification-service | 8090 | `formecertification/certification-service/` |

---

## Prerequisites

- Docker Desktop (Windows) with Kubernetes enabled
- Java 17, Maven 3.9
- Jenkins with plugins: Pipeline, Docker, SonarQube Scanner, Kubernetes CLI
- kubectl configured

---

## Local Development

**mentor-service:**
```bash
mvn spring-boot:run
# Runs on http://localhost:8088
```

**certification-service:**
```bash
cd formecertification/certification-service
mvn spring-boot:run -Dspring.profiles.active=dev
# Runs on http://localhost:8090
```

---

## Docker Compose (Full Stack)

```bash
# Edit .env with your secrets
docker-compose up --build

# Stop
docker-compose down -v
```

Services started: mentor (8091), certification (8090), frontend (80), SonarQube (9000), Prometheus (9090), Grafana (3000).

---

## Environment Variables (.env)

| Variable | Default |
|---|---|
| DB_PASSWORD | *(empty)* |
| DB_USERNAME | root |
| DB_NAME | forme_certification1 |
| JWT_SECRET | see .env |
| APP_JWT_SECRET | see .env |
| MAIL_USERNAME | *(empty)* |
| MAIL_PASSWORD | *(empty)* |
| GOOGLE_AI_API_KEY | *(empty)* |

---

## Jenkins CI Setup

Add these credentials in Jenkins (Manage Jenkins → Credentials):

| ID | Type | Value |
|---|---|---|
| dockerhub-credentials | Username/Password | DockerHub login |
| sonar-token | Secret text | SonarQube token |
| kubeconfig | Secret file | ~/.kube/config |

Configure SonarQube server: **Manage Jenkins → Configure System → SonarQube servers**, name: `SonarQube`.

Create pipelines (Pipeline from SCM, branch `back`):
- mentor CI: script path `Jenkinsfile`
- certification CI: script path `formecertification/certification-service/Jenkinsfile`
- CD: script path `Jenkinsfile-CD`

---

## Kubernetes Deployment

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secrets/
kubectl apply -f k8s/deployments/
kubectl apply -f k8s/services/
kubectl apply -f k8s/monitoring/

# Verify
kubectl get pods -n pidev
kubectl get svc -n pidev
```

NodePort access:
- Prometheus: `http://<node-ip>:30090`
- Grafana: `http://<node-ip>:30300` (admin/admin123)

---

## Health Checks

```bash
# Docker Compose
curl http://localhost:8090/actuator/health
curl http://localhost:8091/actuator/health

# Kubernetes
kubectl port-forward svc/certification-service 8090:8090 -n pidev
curl http://localhost:8090/actuator/health
```

---

## Project Structure

```
FormeBack1/
├── src/                          # mentor-service source
├── Dockerfile                    # mentor-service Docker build
├── Jenkinsfile                   # mentor-service CI pipeline
├── Jenkinsfile-CD                # Kubernetes CD pipeline
├── docker-compose.yml
├── prometheus.yml
├── .env
├── k8s/
│   ├── namespace.yaml
│   ├── secrets/
│   ├── deployments/
│   ├── services/
│   └── monitoring/
└── formecertification/
    └── certification-service/
        ├── src/
        ├── Dockerfile
        └── Jenkinsfile           # certification CI pipeline
```
