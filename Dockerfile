FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu AS builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src/ src/
RUN ./mvnw -q -DskipTests clean package

FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu

WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring --create-home spring

COPY --from=builder /app/target/*.jar /app/app.jar

RUN mkdir -p /app/logs && chown -R spring:spring /app

USER spring

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
