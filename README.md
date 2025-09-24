# Fivlo Backend

📌 **Fivlo Backend**는 [Spring Boot](https://spring.io/projects/spring-boot)를 기반으로 한 백엔드 애플리케이션입니다.  
JWT 인증, 소셜 로그인, DB 마이그레이션(Flyway) 등을 포함한 **실제 서비스 운영 수준**의 프로젝트를 목표로 하고 있습니다.

---

## 🚀 주요 기능
- 회원가입 & 로그인 (자체 로그인 + 소셜 로그인)
- JWT 기반 인증 & 리프레시 토큰
- Batch 작업을 통한 회원 관리
- Flyway 기반 DB 마이그레이션
- REST API 제공
- Docker 기반 배포

---

## 🛠 기술 스택
- **Backend**: Spring Boot 3.x, Spring Security, JPA (Hibernate)
- **DB**: PostgreSQL, Redis
- **Authentication**: JWT, OAuth2 (Google, Kakao)
- **Deployment**: Docker, Docker Compose
- **Version Control**: Git, GitHub

---

## 📂 프로젝트 구조
```bash
src/
 ├── main/
 │   ├── java/com/example/fivlo
 │   │    ├── config/         # 설정
 │   │    ├── domain/         # 엔티티 & 레포지토리 & 비즈니스 로직
 │   │    ├── common/        # AI & Scheduler & API 엔드포인트 관리
 │   │    └── security/     # JWT & OAUTH2 & Filter
 │   └── resources/
 │        ├── application.yml # 환경 설정
 │        └── db/migration/   # Flyway 마이그레이션 파일
 └── test/                    # 테스트 코드

---

## ⚙️ 실행방법
1. 환경 변수 설정
```bash

