# FIVLO Backend Dockerfile
# 전문가 수정안 적용: curl 없는 JRE 이미지 사용, secrets 바인드 마운트

# =========================
# ✅ Dockerfile 수정
# =========================

# -------------------- 1. 빌드(Build) 스테이지 --------------------
# Gradle과 JDK가 설치된 이미지를 'builder'라는 이름으로 사용
FROM gradle:8.5-jdk17-alpine AS builder

# 작업 디렉토리를 /app 으로 설정
WORKDIR /app

# git pull로 받은 최신 소스코드 전체를 복사
COPY . .

# gradlew 파일에 실행 권한 부여
RUN chmod +x ./gradlew

# Gradle을 사용해 프로젝트를 빌드
# 이 명령어가 build/libs/ 안에 최신 .jar 파일을 생성
RUN ./gradlew build -x test


# -------------------- 2. 실행(Runtime) 스테이지 --------------------
# 실제 앱을 실행할 때는 JRE 이미지를 사용
FROM eclipse-temurin:17-jre

# 작업 디렉토리를 /app 으로 설정
WORKDIR /app

# 'builder' 스테이지에서 방금 생성한 .jar 파일을 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트를 노출합니다.
EXPOSE 8080

# 최종 애플리케이션을 실행합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]