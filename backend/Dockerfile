FROM jelastic/maven:3.9.5-openjdk-21 AS build

WORKDIR /app

COPY ./pom.xml /app
COPY ./src /app/src

RUN mvn clean
RUN mvn --version
RUN mvn formatter:format
RUN mvn package -Dmaven.test.skip=true

FROM openjdk:21-jdk

COPY /src/main/resources/application.properties /app/application.properties

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
