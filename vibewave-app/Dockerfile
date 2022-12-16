FROM openjdk:19
ADD target/vibewave-app.jar /app/vibewave-app.jar
ADD src/main/resources/application.properties /app/application.properties
ADD google-credentials.json /app/google-credentials.json
ENTRYPOINT ["java" ,"-Dspring.config.location=classpath:file:/app/application-properties","-jar","/app/vibewave-app.jar"]
EXPOSE 8080