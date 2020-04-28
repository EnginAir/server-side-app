FROM maven:3-jdk-11-slim AS build

WORKDIR /app/build

COPY . .

RUN [ "mvn", "package" ]

RUN [ "ls", "target/" ]

RUN [ "cp", "target/server-side-app-1.0-SNAPSHOT.jar", "../server-side-app.jar" ]

WORKDIR /app

RUN [ "cp", "build/bootstrap.sh", "./" ]

RUN [ "rm", "-rf", "build" ]

FROM openjdk:12-alpine

WORKDIR /app

COPY --from=build /app/ /app

CMD [ "bootstrap.sh" ]
