FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY gym-server/pom.xml gym-server/
COPY springai-model/pom.xml springai-model/
RUN mvn dependency:go-offline -pl gym-server -am
COPY . .
RUN mvn clean package -pl gym-server -am -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/gym-server/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
