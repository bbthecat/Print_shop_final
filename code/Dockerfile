FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace/app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:17-jre-jammy
COPY --from=build /workspace/app/target/*.jar app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
