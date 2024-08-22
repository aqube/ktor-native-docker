FROM ktor-native:build AS build
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN gradle shadowJar --no-daemon

FROM bellsoft/liberica-openjdk-alpine-musl:21
EXPOSE 8080:8080
COPY --from=build /app/build/libs/ktor-native-docker-1.0-SNAPSHOT-all.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]