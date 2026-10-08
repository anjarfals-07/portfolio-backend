# ============================================================
# STAGE 1 — BUILD dengan Maven
# ============================================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================
# STAGE 2 — RUNTIME
# ============================================================
FROM eclipse-temurin:17-jre-alpine

# ⭐ Install packages (tanpa pg client — pakai Cloudinary untuk file)
RUN apk add --no-cache bash tzdata wget

ENV TZ=Asia/Jakarta
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /tmp/backups && chmod 755 /tmp/backups

RUN addgroup -S portfolio && adduser -S portfolio -G portfolio
RUN chown -R portfolio:portfolio /app /tmp/backups
USER portfolio

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

CMD ["java", "-jar", "app.jar"]