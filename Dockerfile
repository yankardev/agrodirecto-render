# Una sola imagen parametrizable para los 4 servicios:  docker build --build-arg MODULE=auth-service .
FROM maven:3.9-eclipse-temurin-17 AS build
ARG MODULE
WORKDIR /app
COPY . .
RUN mvn -B -q -pl ${MODULE} -am package -DskipTests

FROM eclipse-temurin:17-jre
ARG MODULE
WORKDIR /app
COPY --from=build /app/${MODULE}/target/${MODULE}-1.0.0.jar app.jar
RUN useradd --system --no-create-home agro
USER agro
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
