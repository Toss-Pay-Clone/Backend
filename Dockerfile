FROM openjdk:17-alpine
ARG JAR_FILE=/build/libs/*.jar
COPY ${JAR_FILE} /toss_server.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "/toss_server.jar"]