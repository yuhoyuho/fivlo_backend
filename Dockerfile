# FIVLO Backend Dockerfile
# 전문가 수정안 적용: curl 없는 JRE 이미지 사용, secrets 바인드 마운트
FROM eclipse-temurin:17-jre

WORKDIR /app

# JAR 파일 복사
COPY build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
