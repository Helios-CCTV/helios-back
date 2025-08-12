# Step 1: Gradle 빌드 단계
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# 의존성 캐시 최적화를 위한 단계
COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle dependencies || true

# 전체 프로젝트 복사 및 빌드
COPY . .
RUN gradle clean build -x test

# Step 2: 실행 단계
FROM openjdk:17-jdk-slim

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=build /app/build/libs/*.jar app.jar

# HTTPS 포트 노출
EXPOSE 8081

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]