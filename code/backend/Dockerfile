FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/components ./components
COPY --from=build /build/target/lib ./lib
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=web,postgres
ENTRYPOINT ["java", "-cp", "components/*:lib/*", "ru.bookstore.Application"]
