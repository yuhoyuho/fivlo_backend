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
```

---

## ⚙️ 실행방법
1. 환경 변수 설정
- .env 파일을 생성하고 다음 값을 추가하세요.
```bash
DB_URL=jdbc:postgresql://db:5432/fivlo
DB_USER=fivlo_user
DB_PASSWORD=yourpassword

JWT_SECRET=your_jwt_secret_key
```

2. Docker Compose 실행
```bash
docker compose up -d --build
```

3. API 확인
- 서버가 정상적으로 실행되면 postman에서
 - API : ``http://localhost:8080/api/v1/~``

4. DB migration 관리 (Flyway)
- 새로운 SQL 스크립트를 추가하고 싶은 경우, ``src/main/resources/db/migration``에 ``V{version_number}__{description}.sql``을 작성하세요.
- 예시
 - ``V4__add_profile_table.sql``
