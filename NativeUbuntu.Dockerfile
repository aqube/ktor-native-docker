FROM ktor-native:build AS build
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN gradle linkReleaseExecutablePlatform --no-daemon --stacktrace --info

FROM ubuntu:latest
EXPOSE 8080:8080
COPY --from=build /app/build/bin/platform/releaseExecutable/ktor-native-docker.kexe /app.kexe

ENTRYPOINT ["/app.kexe"]