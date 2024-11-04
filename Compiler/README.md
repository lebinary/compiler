<!-- PROJECT HEADER -->
<div align="center">
  <h1>A Compiler</h1>
  <p align="center">
    A compiler that translates a Java-like language into SaM assembly code for educational purposes.
  </p>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#built-with">Built With</a>
    </li>
    <li>
      <a href="#features">Features</a>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li>
      <a href="#development">Development</a>
      <ul>
        <li><a href="#building-jar">Building Jar</a></li>
        <li><a href="#running-tests">Running Tests</a></li>
      </ul>
    </li>
    <li>
      <a href="#usage">Usage</a>
    </li>
    <li><a href="#implementation-details">Implementation Details</a></li>
  </ol>
</details>

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
```shell
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
