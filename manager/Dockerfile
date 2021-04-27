FROM jre11
ARG APP_JAR_FILE=target/*.jar
COPY ${APP_JAR_FILE} application.jar

# This Dockerfile assumes that ES is running externally.
ENTRYPOINT ["java", "-jar", "application.jar"]