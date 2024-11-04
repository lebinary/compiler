# A Compiler

A compiler that translates a Java-like language into SaM assembly code for educational purposes.

## Table of Contents
- [Built With](#built-with)
- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Development](#development)
  - [Building Jar](#building-jar)
  - [Running Tests](#running-tests)
- [Usage](#usage)
- [Implementation Details](#implementation-details)

## Built With

- Java
- SaM Assembly

## Features

- Multi-pass compilation process
- Comprehensive symbol table management
- Type checking and error handling
- Support for:
  - Basic arithmetic and logical operations
  - Control flow statements (if-else, while loops)
  - Method declarations and invocations
  - String operations and manipulation
  - Object-oriented programming constructs (no inheritance yet)

## Getting Started

### Prerequisites

- Java JDK 8 or higher
- SaM Assembler and Runtime

### Installation

1. Clone the repository
   ```sh
   git clone [repository-url]
   ```

2. Compile the Java source files
   ```sh
   javac *.java
   ```

## Development

This starter code package includes a gradle build configuration and JUnit test cases to help you in completing the assignment.
You should follow the gradle installation instructions for your system at [https://gradle.org/install/](https://gradle.org/install/)
and make sure you have Java version 11 or later installed as the build file targets Java 11.

### Building Jar

You can build the JAR file with
```sh
gradle build
```
which will leave the jar file at `build/libs/compiler.jar`

If you have some tests failing and want to build a jar anyway, you can skip tests using
```sh
gradle build -x test
```

On some platforms you may need to use the included `gradlew` or `gradlew.bat` scripts instead of calling `gradle` directly.

### Running Tests

Any IDE with gradle support can run the test cases, but they can also be run manually from the commandline by

```sh
gradle test
```

## Usage

1. Prepare a source code file (e.g., `program.lo`)
   Example source code:
   ```java
   int main() {
       String message;
       message = "Hello, World!";
       return 0;
   }
   ```

2. Build jar file of the compiler, a `compiler.jar` file should be created in `/build/libs`
   ```sh
   gradle build --rerun-tasks
   ```

3. Run the jar file to generate a SAM file
   ```sh
   java -jar compiler.jar program.lo output.sam
   ```

4. Start the Stack Abstract Machine simulator
   ```sh
   java -Dfile.encoding=UTF8 -jar SaM-2.6.3.jar
   ```

5. Load the generated SAM file `output.sam` and run the simulator!

## Implementation Details

### Compiler Structure

The compiler implements a two-pass compilation strategy:

1. **First Pass**: Symbol Table Population
   - Collects variable and method declarations
   - Performs scope analysis
   - Validates declarations and signatures

2. **Second Pass**: Code Generation
   - Generates SaM assembly code
   - Performs type checking
   - Manages memory allocation
   - Handles control flow
