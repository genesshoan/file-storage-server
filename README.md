# File Storage Server

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)]()

A multithreaded file storage server built from scratch using pure Java. This project implements a robust TCP server that handles concurrent file operations through socket programming and thread pooling. File metadata is managed using SQLite with HikariCP for efficient connection pooling.

> **Note:** This is a learning project based on the [File Server project from JetBrains Academy (Hyperskill)](https://hyperskill.org/projects/52). It demonstrates practical implementation of networking, concurrency, database integration, and software design patterns in Java.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technologies & Dependencies](#technologies--dependencies)
- [Project Structure](#project-structure)
- [Installation](#installation)
- [Usage](#usage)
  - [Starting the Server](#starting-the-server)
  - [Console Menu](#console-menu)
  - [Available Operations](#available-operations)
- [Configuration](#configuration)
- [Testing](#testing)
- [Architecture & Design Patterns](#architecture--design-patterns)
- [Future Improvements](#future-improvements)
- [Credits & Acknowledgments](#credits--acknowledgments)
- [License](#license)

## Overview

The File Storage Server is a console-based application that provides a file storage service over TCP. It showcases fundamental concepts in network programming, concurrent systems, and database integration.

**Key Learning Objectives:**
- Implementing socket programming with Java's `ServerSocket` and `Socket` classes
- Managing concurrent connections using thread pools (`ExecutorService`)
- Database integration with connection pooling (HikariCP + SQLite)
- Applying Object-Oriented Programming (OOP) principles
- Implementing design patterns (Singleton, Strategy)
- Separating business logic from user interface
- Building robust error handling and logging mechanisms

## Features

- ✅ **Multithreaded Architecture**: Handles multiple concurrent TCP connections using a configurable thread pool
- ✅ **File Operations**: PUT (upload), GET (download), and DELETE file operations via custom protocol
- ✅ **Database Integration**: Stores file metadata in SQLite with HikariCP connection pooling
- ✅ **Interactive Console**: Menu-driven interface to start, stop, and monitor server status
- ✅ **Thread-Safe Operations**: Ensures data integrity across concurrent requests
- ✅ **Configurable**: External properties files for server and storage configuration
- ✅ **Comprehensive Logging**: SLF4J-based logging for monitoring and debugging
- ✅ **Robust Testing**: Unit tests with Mockito and AssertJ

## Technologies & Dependencies

### Runtime Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| **SQLite JDBC** | 3.45.3.0 | JDBC driver for SQLite database operations |
| **HikariCP** | 5.0.1 | High-performance JDBC connection pool for managing database connections efficiently |
| **SLF4J API** | 1.7.36 | Simple Logging Facade for Java - provides abstraction for logging |
| **JetBrains Annotations** | 24.1.0 | Annotations for code documentation and static analysis (@NotNull, @Nullable, etc.) |

### Testing Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| **JUnit Jupiter** | 5.10.0 | Modern testing framework for Java (JUnit 5) |
| **AssertJ** | 3.26.0 | Fluent assertions library for more readable test assertions |
| **Mockito Core** | 5.5.0 | Mocking framework for unit tests |
| **Mockito JUnit Jupiter** | 5.5.0 | Integration between Mockito and JUnit 5 |

## Project Structure

```
file-storage-server/
├── src/
│   ├── main/
│   │   ├── java/dev/shoangenes/
│   │   │   ├── config/              # Configuration management
│   │   │   │   ├── PropertiesLoader.java
│   │   │   │   ├── ServerProperties.java
│   │   │   │   ├── StorageProperties.java
│   │   │   │   └── LoggerProperties.java
│   │   │   ├── exception/           # Custom exceptions
│   │   │   │   └── DatabaseException.java
│   │   │   ├── file/                # File management
│   │   │   │   ├── FileManager.java
│   │   │   │   └── FileAccessManager.java
│   │   │   ├── model/               # Domain models and enums
│   │   │   │   ├── FileMetadata.java
│   │   │   │   ├── FSMessage.java
│   │   │   │   ├── FSHeader.java
│   │   │   │   ├── OpCode.java      # PUT, GET, DELETE operations
│   │   │   │   └── ResultCode.java   # Response codes
│   │   │   ├── repository/          # Data access layer
│   │   │   │   ├── DatabaseManager.java      # Singleton for HikariCP
│   │   │   │   ├── IFileRepository.java
│   │   │   │   └── DBFileRepository.java
│   │   │   ├── server/              # Server core
│   │   │   │   ├── ServerConsole.java        # Entry point & console menu
│   │   │   │   ├── FileServer.java           # TCP server with thread pool
│   │   │   │   ├── RequestHandler.java       # Handles individual connections
│   │   │   │   ├── IFileService.java
│   │   │   │   └── FileService.java          # Business logic
│   │   │   └── utils/               # Utilities
│   │   │       ├── FileValidator.java
│   │   │       ├── LoggerUtil.java
│   │   │       └── LogFilter.java
│   │   └── resources/
│   │       ├── server.properties     # Server configuration
│   │       └── logger.properties     # Logging configuration
│   └── test/                         # Unit tests
│       └── java/dev/shoangenes/
├── build.gradle                      # Gradle build configuration
└── README.md
```

### Key Components

- **ServerConsole**: Entry point with interactive menu for server lifecycle management
- **FileServer**: Core TCP server managing socket connections and thread pool
- **RequestHandler**: Processes individual client requests in separate threads
- **FileService**: Business logic for file operations (PUT, GET, DELETE)
- **DatabaseManager**: Singleton managing HikariCP connection pool for SQLite
- **FileManager**: Handles file system operations and storage
- **FileMetadata**: Domain model representing file information stored in database

## Installation

### Prerequisites

- **Java Development Kit (JDK) 17 or higher**
- **Gradle** (wrapper included)

### Clone the Repository

```bash
git clone https://github.com/genesshoan/file-storage-server.git
cd file-storage-server
```

### Build the Project

```bash
# On Linux/Mac
./gradlew build

# On Windows
gradlew.bat build
```

This will:
- Download all dependencies
- Compile the source code
- Run all tests
- Generate build artifacts

## Usage

### Starting the Server

Run the application using Gradle:

```bash
# On Linux/Mac
./gradlew run

# On Windows
gradlew.bat run
```

Alternatively, build a JAR and run it:

```bash
./gradlew jar
java -jar build/libs/file-storage-server-1.0-SNAPSHOT.jar
```

### Console Menu

Upon starting the application, you'll see an interactive console menu:

```
╔══════════════════════════════╗
║      FILE SERVER v0          ║
╚══════════════════════════════╝

1. Start Server
2. Stop Server
3. View Status
4. Exit
Enter your choice:
```

### Available Operations

#### 1. Start Server
Initializes and starts the file server on the configured port (default: 8080).

```
Enter your choice: 1
Server started.
```

#### 2. Stop Server
Gracefully shuts down the server, closing all connections and releasing resources.

```
Enter your choice: 2
Server stopped.
```

#### 3. View Status
Displays current server status and active connections.

```
Enter your choice: 3
Server Status: Running
Active Connections: 2
```

#### 4. Exit
Stops the server (if running) and exits the application.

```
Enter your choice: 4
Exiting...
```

### Protocol Operations

The server accepts the following file operations via its custom protocol:

- **PUT**: Upload a file to the server
- **GET**: Download a file from the server
- **DELETE**: Remove a file from the server

> **Note:** A client implementation is required to communicate with the server using the protocol. See [Future Improvements](#future-improvements) for client development plans.

## Configuration

### Server Configuration (`server.properties`)

```properties
server.port=8080
server.host=localhost
server.threadPoolSize=20
server.backlog=50
```

- `server.port`: Port number for the server to listen on
- `server.host`: Host address to bind to
- `server.threadPoolSize`: Maximum number of concurrent client connections
- `server.backlog`: Maximum length of the connection queue

### Storage Configuration (`storage.properties`)

Create this file in `src/main/resources/` to customize database settings:

```properties
storage.databaseUrl=jdbc:sqlite:./data/fileserver.db
storage.databaseDriver=org.sqlite.JDBC
```

## Testing

The project includes comprehensive unit tests using modern testing frameworks.

### Run All Tests

```bash
./gradlew test
```

### Testing Tools

- **JUnit 5 (Jupiter)**: Modern test framework with annotations like `@Test`, `@BeforeEach`, `@Nested`
- **AssertJ**: Fluent assertions for readable test code
  ```java
  assertThat(result).isNotNull().hasFieldOrPropertyWithValue("status", 200);
  ```
- **Mockito**: Mocking framework for isolating units under test
  ```java
  @Mock
  private IFileRepository repository;
  ```

### Test Coverage

The project tests critical components:
- File operations (PUT, GET, DELETE)
- Protocol message parsing
- File validation logic
- Result code handling
- OpCode enumeration

## Architecture & Design Patterns

### Design Patterns Implemented

1. **Singleton Pattern**
   - `DatabaseManager`: Ensures a single connection pool instance
   - `ServerProperties`, `StorageProperties`: Single configuration instances

2. **Strategy Pattern**
   - `IFileRepository` interface allows different repository implementations
   - `IFileService` interface separates business logic from implementation

3. **Thread Pool Pattern**
   - Uses `ExecutorService` to manage concurrent client connections efficiently

4. **Separation of Concerns**
   - **Presentation Layer**: `ServerConsole` (UI)
   - **Business Layer**: `FileService` (logic)
   - **Data Layer**: `DBFileRepository` (persistence)
   - **Network Layer**: `FileServer`, `RequestHandler` (communication)

### Concurrency & Thread Safety

- Thread pool limits concurrent connections based on configuration
- HikariCP manages database connections across threads
- Synchronized access to shared resources where necessary
- Proper exception handling to prevent thread interruption

## Future Improvements

### Short-term Enhancements

- [ ] **Client Implementation**: Develop a command-line or GUI client to interact with the server
- [ ] **Authentication & Authorization**: Add user authentication and file access control
- [ ] **Extended Protocol**: Support additional operations (LIST, RENAME, COPY)
- [ ] **File Encryption**: Implement encryption for secure file storage
- [ ] **Compression**: Add file compression to save storage space

### Long-term Goals

- [ ] **REST API**: Expose file operations via HTTP/REST for web integration
- [ ] **Web Interface**: Build a web-based UI for file management
- [ ] **Distributed Storage**: Scale to multiple servers with file replication
- [ ] **Monitoring Dashboard**: Real-time metrics and statistics
- [ ] **Performance Optimization**: Implement caching and async I/O
- [ ] **Docker Support**: Containerize the application for easy deployment
- [ ] **Cloud Storage Integration**: Support for AWS S3, Azure Blob, etc.

## Credits & Acknowledgments

### Inspiration

This project is based on the **File Server project** from [JetBrains Academy (Hyperskill)](https://hyperskill.org/projects/52), an excellent platform for learning Java through practical projects.

### Technologies

- **Java** - The foundation of this project
- **HikariCP** - High-performance JDBC connection pool
- **SQLite** - Lightweight embedded database
- **Gradle** - Build automation tool
- **JUnit 5, Mockito, AssertJ** - Testing frameworks

### Resources

- [Java Socket Programming](https://docs.oracle.com/javase/tutorial/networking/sockets/)
- [Java Concurrency in Practice](https://jcip.net/)
- [HikariCP Documentation](https://github.com/brettwooldridge/HikariCP)

## License

This project is available under the MIT License. See the [LICENSE](LICENSE) file for details.

---

**Developed by:** [genesshoan](https://github.com/genesshoan)  
**Project Link:** [https://github.com/genesshoan/file-storage-server](https://github.com/genesshoan/file-storage-server)

---

*If you found this project helpful for learning, please consider giving it a ⭐ on GitHub!*
