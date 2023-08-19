FROM openjdk:17
COPY rest/target/bff-rest.jar bff.jar
EXPOSE 8082
CMD ["java", "-jar", "bff.jar"]