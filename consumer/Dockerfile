# syntax=docker/dockerfile:1
FROM cgr.dev/chainguard/maven:latest AS build
USER root
WORKDIR /workspace
RUN chown -R nonroot:nonroot /workspace
USER nonroot
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM cgr.dev/chainguard/jre:latest
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=build /workspace/target/consumer-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
