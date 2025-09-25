FROM openjdk:17-jdk-slim
WORKDIR /app
RUN addgroup --system app && adduser --system --ingroup app app
USER app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]