FROM eclipse-temurin:25-jre

RUN useradd --system --uid 1001 puriy
COPY build/libs/app.jar /app/puriy.jar
USER puriy

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/puriy.jar"]
