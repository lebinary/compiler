# lo2-to-sam-tester
SIMPL Summer 2022 - Live Oak 2 to SAM Tester

## Gradle Installation

This starter code package includes a gradle build configuration and JUnit test cases to help you in completing the assignment.
You should follow the gradle installation instructions for your system at [https://gradle.org/install/](https://gradle.org/install/)
and make sure you have Java version 11 or later installed as the build file targets Java 11.

## Building Jar

You can build the JAR file with 
```sh
gradle build
```
which will leave the jar file at `build/libs/compiler.jar`

If you have some tests failing and want to build a jar anyway, you can skip tests using
```shell
gradle build -x test
```

On some platforms you may need to use the included `gradlew` or `gradlew.bat` scripts instead of calling `gradle` directly.

## Running Tests

Any IDE with gradle support can run the test cases, but they can also be run manually from the commandline by

```sh
gradle test
```

