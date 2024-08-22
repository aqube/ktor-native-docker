FROM gradle:8.9.0-jdk21
COPY --chown=gradle:gradle *.gradle.kts gradle.properties /app/
COPY --chown=gradle:gradle src /app/src
WORKDIR /app
RUN gradle compileKotlinPlatform --stacktrace --info --build-cache