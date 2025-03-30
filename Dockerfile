FROM openjdk:17-jdk
COPY build/libs/DynamicClassChange-1.0.0-SNAPSHOT.jar /opt/app.jar
CMD ["java", "-jar", "/opt/app.jar"]