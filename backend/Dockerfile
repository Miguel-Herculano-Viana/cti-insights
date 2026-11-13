# Build no Render: "Root Directory" vazio (raiz do repo) e "Dockerfile Path" = backend/Dockerfile
# (precisa da raiz porque o pom.xml usa ../database/schema.sql)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY database/schema.sql database/schema.sql
COPY backend/pom.xml backend/pom.xml
RUN mvn -q -f backend/pom.xml dependency:go-offline
COPY backend/src backend/src
RUN mvn -q -f backend/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/backend/target/cti-insights-api-1.0.0.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
