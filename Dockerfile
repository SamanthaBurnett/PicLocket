# ---------- Build Stage ----------
FROM azul/zulu-openjdk:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

COPY src src

RUN ./gradlew bootJar --no-daemon

# ---------- Runtime Stage ----------
FROM azul/zulu-openjdk:21-jre

WORKDIR /app

RUN addgroup --system app && \
    adduser --system --ingroup app app

COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown app:app app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]