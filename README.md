# ForME – Professional Training & Certification Platform

## Overview

This project was developed as part of the PIDEV – 2nd Year Engineering Program at **Esprit School of Engineering** (Academic Year 2025–2026).

ForME is a full-stack, AI-powered e-learning platform designed to help professionals and learners discover training programs, earn certifications, track their progress, and receive personalized mentoring. The platform connects learners, administrators, and partners through a unified ecosystem built on a modern microservices architecture.

## Features

- **Authentication & Role Management** – JWT-based authentication with role hierarchy (SuperAdmin, Admin, User)
- **Formation Catalog** – Browse, enroll in, and complete training programs with AI-assisted content generation
- **Exams & Evaluations** – Take exams, get scored results, and view detailed performance analytics
- **Certification** – Automatic PDF certificate generation upon successful course completion
- **AI Mentor** – Personalized AI-powered mentor (powered by Google Gemini) offering adaptive study plans, skill gap analysis, learning streak tracking, and portfolio management
- **Events Management** – Create and manage professional events with partner sponsorship and tiered participation
- **Admin Dashboard** – Real-time analytics, learner funnel visualization, geographic learner map, AI insights, marketing content creator, and RAG-based assistant
- **Notifications** – In-app notification system for learners and admins
- **Formation Demands** – Learners can request new training topics reviewed by admins

## Tech Stack

### Frontend

- Angular 17+ (TypeScript)
- Bootstrap & Bootstrap Icons
- Chart.js (analytics charts)
- Angular Router with lazy-loaded feature modules

### Backend

- Java 17
- Spring Boot 3.2
- Spring Cloud 2023 (Eureka, API Gateway)
- Spring Security + JWT
- Spring Data JPA
- MySQL 8
- Google Gemini AI (generative AI features)
- iText / PDF generation (certification service)
- Docker & Docker Compose

## Architecture

ForME follows a **microservices architecture** with the following services:

| Service | Port | Responsibility |
|---|---|---|
| `eureka-server` | 8761 | Service discovery & registry |
| `api-gateway` | 8080 | Single entry point, routing & JWT validation |
| `user-service` | 8081 | User registration, login, role management |
| `formation-service` | — | Training catalog, exams, evaluations, AI generation |
| `events-service` | — | Professional events, partners, participants, deposits |
| `certification-service` | — | Certificate issuance and PDF generation |
| `mentor-service` | — | AI mentor, study plans, streaks, portfolios, skill tracking |

All services register with Eureka and communicate through the API Gateway. The system is fully containerized with Docker Compose, including a managed MySQL instance with health-checked startup ordering.

```
Client (Angular)
      │
      ▼
 API Gateway (:8080)
      │
      ├──► User Service
      ├──► Formation Service
      ├──► Events Service
      ├──► Certification Service
      └──► Mentor Service
                │
         Eureka Server (:8761)
                │
           MySQL (:3306)
```

## Contributors

| Name | Role |
|---|---|
| — | User & Authentication Service |
| — | Formation & Evaluation Service |
| — | Events Service |
| — | Certification Service |
| — | Mentor & AI Service |
| — | Frontend (Angular) |

## Academic Context

Developed at *Esprit School of Engineering – Tunisia*
PIDEV – 3A | 2025–2026

## Getting Started

### Prerequisites

- Docker & Docker Compose installed
- Node.js 18+ and npm (for the frontend)

### Run the Backend (Docker Compose)

```bash
cd FormeBack/FormeBack
docker compose up --build
```

This will start MySQL, Eureka Server, API Gateway, and User Service in the correct order.

### Run the Frontend

```bash
cd FormeFront
npm install
npm start
```

The frontend will be available at `http://localhost:4200`.

### Default Ports

| Service | URL |
|---|---|
| Frontend | http://localhost:4200 |
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |

## Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) – Backend framework
- [Spring Cloud Netflix Eureka](https://spring.io/projects/spring-cloud-netflix) – Service discovery
- [Angular](https://angular.io/) – Frontend framework
- [Google Gemini](https://deepmind.google/technologies/gemini/) – Generative AI features
- [Docker](https://www.docker.com/) – Containerization
- Esprit School of Engineering – for the academic framework and support
