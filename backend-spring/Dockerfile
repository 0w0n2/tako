# ---------- Build stage ----------
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# (선택) 테스트 스킵 여부를 빌드 인자로 제어
ARG SKIP_TESTS=true

# Gradle 래퍼/의존성만 먼저 복사해서 캐시 극대화
COPY gradlew settings.gradle* build.gradle* gradle.properties* ./
COPY gradle ./gradle
RUN chmod +x gradlew

# BuildKit 캐시를 이용해 Gradle 캐시 보존 (DOCKER_BUILDKIT=1 필요)
# gradle 캐시 경로: /root/.gradle (root 기준)
RUN --mount=type=cache,target=/root/.gradle ./gradlew --version

# 이후 소스 복사
COPY . .

# 의존성/컴파일 캐시 활용 + 테스트 스킵 옵션
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew clean bootJar $( [ "$SKIP_TESTS" = "true" ] && echo "-x test" )

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# 보안: 비루트 유저
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring

# 빌드 산출물 복사
COPY --from=build /app/build/libs/*.jar /app/app.jar

# 포트만 노출 (환경변수/옵션은 compose에서 관리)
EXPOSE 8080

ENV JAVA_OPTS=""

# 깔끔한 기본 엔트리포인트
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
