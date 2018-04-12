FROM openjdk:8-jdk-slim as runtime

WORKDIR /srv
COPY . .
RUN ./gradlew test fatJar

ENTRYPOINT ["java"]
CMD ["-jar", "build/libs/echo-fat.jar"]
