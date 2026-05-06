# FormeBack DevOps

## Overview

This repository contains the Spring Boot backend microservices for ForME.

- Parent project: repository root
- User service path: `user-service/`
- Docker image: `samiwin/user-service:latest`
- Jenkins backend CI pipeline: `Jenkinsfile-user-service`
- Kubernetes CD is centralized in the `Formedevops` repository

## Local Commands

Run backend tests from the user service module:

```bash
cd user-service
mvn test
```

## Coverage

JaCoCo generates reports under:

```text
user-service/target/site/jacoco/
user-service/target/site/jacoco/jacoco.xml
```

## Docker

Build the backend image:

```bash
docker build -t samiwin/user-service:latest -f user-service/Dockerfile .
```

## Jenkins Backend CI

`Jenkinsfile-user-service` performs:

- checkout
- Maven package
- Maven tests
- JaCoCo coverage report generation
- optional SonarQube analysis
- Docker build and push for `samiwin/user-service:latest`

## CD Note

Kubernetes deployment, monitoring, and centralized CD remain in the `Formedevops` repository.

