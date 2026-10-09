FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace/app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:17-jre-jammy
COPY --from=build /workspace/app/target/*.jar app.jar
ENV PORT=8080
# ร้านอยู่ไทย: วันที่ในรายงาน/เวลาสั่งงานต้องเป็นเวลาไทย ไม่ใช่ UTC ของ server
ENV TZ=Asia/Bangkok
EXPOSE 8080
ENTRYPOINT ["java","-Duser.timezone=Asia/Bangkok","-jar","/app.jar"]
