# Ktor Native Docker

Minimal example Ktor project with `native` platform and `jvm` targets and docker images. 

**Built using:**

- [Kotlin](https://kotlinlang.org/docs/home.html)
  - [Native](https://kotlinlang.org/docs/native-overview.html)
  - [Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Ktor](https://ktor.io):
  - [Native Server](https://ktor.io/docs/server-native.html)
  - [HTML DSL](https://ktor.io/docs/server-html-dsl.html) with [kotlinx.html](https://github.com/Kotlin/kotlinx.html)
  - [Serialization](https://ktor.io/docs/server-serialization.html)
  - [CIO Engine](https://ktor.io/docs/server-engines.html#supported-engines)
- [Gradle Shadow](https://github.com/GradleUp/shadow)
- [Docker](https://docs.docker.com/)

## Source Sets & Project Structure

There are 6 source sets in the `src/` directory.
`nativeMain` and `jvmMain` contain platform specific code for the jvm and platform specific native targets.
All code shared between targets is in `commonMain`.
`*Test` sources exist for each `*Main` source set.   
See https://kotlinlang.org/docs/multiplatform-hierarchy.html` for more information.

`module.kt` in `commonMain` implements an example [Ktor module](https://ktor.io/docs/server-modules.html).
It defines 4 routes:

- `GET /` serves html templated using the [HTML DSL](https://ktor.io/docs/server-html-dsl.html).
- `GET /health` empty response with status code `200 OK`.
- `GET /platform` responds with a simple json containing either `Native` or `JVM` depending on the platform.
  Uses [Ktor content negotiation and serialization](https://ktor.io/docs/server-serialization.html).
- `POST /hash` responds with the `SHA3_256` hash of the request body.

`Main.kt` in `nativeMain` and `jvmMain` contains the configuration of the `embeddedServer` for each platform.
They both use the `CIO` engine, but the JVM engine can be swapped
with [other supported engines](https://ktor.io/docs/server-engines.html#supported-engines).

## ⚒️ Build & Run

Compile and run the native binary for the current platform:   
`./gradlew compileKotlinPlatform`     
`./gradlew runReleaseExecutablePlatform`  

Build and run the uber/shadow Jar:  
`./gradlew shadowJar`  
`./gradlew runShadow` 

Execute Tests:  
`./gradlew allTests`  
`./gradlew jvmTest`  
`./gradlew platformTest`  

## 🐳 Docker Images

With the `--platform linux/amd64` flag, we can build images for x86 targets on ARM.

### Native Docker Images

To build the native image we (once) build a build image.
This is very large (~2.8GB) and will download platform specific kotlin native dependencies.
Building a separate image to cache these dependencies makes subsequent builds much faster.
The build image only has to be updated when dependencies change.

```shell
docker buildx build --platform linux/amd64 -f NativeBuild.Dockerfile -t ktor-native:build .
```

The actual `ktor-native` image can then be built:

```shell
docker buildx build --platform linux/amd64 -f NativeAlpine.Dockerfile -t ktor-native:alpine .
```

```shell
docker buildx build --platform linux/amd64 -f NativeDebian.Dockerfile -t ktor-native:debian .
```

```shell
docker buildx build --platform linux/amd64 -f NativeUbuntu.Dockerfile -t ktor-native:ubuntu .
```

and then run with:

```shell
docker run --platform linux/amd64 -p 8080:8080 --rm ktor-native:alpine
```

```shell
docker run --platform linux/amd64 -p 8080:8080 --rm ktor-native:debian
```

```shell
docker run --platform linux/amd64 -p 8080:8080 --rm ktor-native:ubuntu
```

### JVM Docker Image

For comparison a JVM image is built using:

```shell
docker buildx build --platform linux/amd64 -f JVMAlpine.Dockerfile -t ktor-native:jvm-alpine .
```

and then run with:

```shell
docker run --platform linux/amd64 -p 8080:8080 --rm ktor-native:jvm-alpine
```

## 📝 Notes

- The "native target" is called `platform` and not `native` like described in the [Ktor native server docs](https://ktor.io/docs/server-native.html#native-target), as `native` leads to this warning: 
  
  > The Default Kotlin Hierarchy Template was not applied to 'root project 'ktor-native-docker'\':
  > Source sets created by the following targets will clash with source sets created by the template:
  > [native]
  > 
  > Consider renaming the targets or disabling the default template by adding
  > 'kotlin.mpp.applyDefaultHierarchyTemplate=false'
  > to your gradle.properties
  > 
  > Learn more about hierarchy templates: https://kotl.in/hierarchy-template


- Calling `./gradlew run` fails with `Error: Could not find or load main class MainKt`. 
  This currently only allows the jvm application to be run using the shadowJar built for the jvm target.
  Some configuration is missing on how to configure the main class from the jvmMain source set for the application. 

- (On Mac) We have to compile the native binary in an x86 linux container. On a `linux/arm64` build
  without the `--platform linux/amd64` flag, we get `Could not find kotlin-native-prebuilt-2.0.0-linux-aarch64.tar.gz`.
  Even though `linuxArm64` is a [supported tier 2 native target](https://kotlinlang.org/docs/native-target-support.html#tier-2).
