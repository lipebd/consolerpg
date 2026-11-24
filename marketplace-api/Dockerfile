FROM eclipse-temurin:25-jdk AS build
WORKDIR /build
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN sh ./mvnw -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/target/marketplace-api-0.0.1-SNAPSHOT.jar app.jar
ENV SERVER_ADDRESS=0.0.0.0 \
    SERVER_PORT=10000 \
    SPRING_DATASOURCE_URL="jdbc:h2:mem:marketplace-demo;DB_CLOSE_DELAY=-1" \
    SPRING_H2_CONSOLE_ENABLED=false
EXPOSE 10000
CMD ["java", "-XX:MaxRAMPercentage=55.0", "-jar", "app.jar"]