# Step 1: Base image 선택
FROM openjdk:17-jdk-slim

# Step 2: Tini 설치
RUN apt-get update && apt-get install -y tini procps net-tools

# Step 3: 작업 디렉토리 설정
WORKDIR /app

# Step 4: JAR 파일 복사
COPY build/libs/*.jar app.jar

# Step 5: 포트 노출
EXPOSE 8080

# Step 6: 환경변수 설정
ENV SPRING_PROFILES_ACTIVE=docker


COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

ENTRYPOINT ["tini", "-s", "--", "/usr/local/bin/docker-entrypoint.sh"]