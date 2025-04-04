# Micronaut MCP Cli

[![Build Status](https://github.com/satai/mcp-micronaut-poc/actions/workflows/gradle.yml/badge.svg)](https://github.com/satai/mcp-micronaut-poc/actions/workflows/gradle.yml)

PoC for [MCP (Model Context Protocol)](https://modelcontextprotocol.io/) integration into [Micronaut](https://micronaut.io/) framework.

## Overview

This project demonstrates how to integrate the Model Context Protocol (MCP) with the Micronaut framework. It provides a way to expose Micronaut beans as MCP tools that can be called by AI models or other MCP clients.

## Installation

### Prerequisites

- JDK 17 or higher

### Building the Project

```bash
./gradlew build
```

### Running the Application

```bash
./gradlew run
```

## Usage

### Creating an MCP Tool

1. Create a Micronaut bean with methods annotated with `@Tool`:

```kotlin
@Singleton
class MyTool {
    @Tool(
        name = "myTool",
        description = "A tool that does something useful"
    )
    fun doSomething(
        @ToolArg(description = "First parameter") param1: String,
        @ToolArg(description = "Second parameter") param2: Int
    ): String {
        return "Result: $param1 $param2"
    }
}
```

2. The tool will be automatically registered with the MCP server when the application starts.

3. Clients can call the tool using the MCP protocol over standard input/output.

## Project Structure

- `MicronautMcpCliCommand.kt` - Main command class that sets up the MCP server
- `ServerWrapper.kt` - Wrapper for the MCP server
- `ToolBuilder.kt` - Processes methods annotated with `@Tool` and registers them as MCP tools
- `ToolAnnotations.kt` - Defines the `@Tool` and `@ToolArg` annotations
- `TypeConverter.kt` - Converts JDK types to MCP types
- `ArgumentConverter.kt` - Converts MCP arguments to JDK types
- `FooTool.kt` - Example tool implementation

## Limitations

- Not a library now, just example App
- CLI (StdInOut) communication only
- Tools only (no Prompts...)
- Only int, boolean, number and strings and arrays (one or multidimensional) as parameters
- Only one String on output, no pictures, music
- no configuration of the server
- Temporary (?) package

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the [Apache License 2.0](LICENSE).
