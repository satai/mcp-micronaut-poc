package cz.nekola.mcpnaut.demo.cli;

import io.modelcontextprotocol.sdk.model.CallToolRequest; // Assuming from Java SDK
import io.modelcontextprotocol.sdk.model.CallToolResult;  // Assuming from Java SDK
import io.modelcontextprotocol.sdk.model.Implementation;  // Assuming from Java SDK
import io.modelcontextprotocol.sdk.model.ServerCapabilities; // Assuming from Java SDK
import io.modelcontextprotocol.sdk.model.ServerOptions;    // Assuming from Java SDK
import io.modelcontextprotocol.sdk.model.Tool;             // Assuming from Java SDK
import io.modelcontextprotocol.sdk.server.McpServer;       // Main Java SDK Server class

import jakarta.inject.Singleton;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Singleton
public class ServerWrapper {

    // Package-private (equivalent to Kotlin's internal for this module)
    final McpServer server;

    public ServerWrapper() {
        Implementation implementation = Implementation.newBuilder()
            .setName("mcp-java-test-server") // Updated name for Java version
            .setVersion("0.1.0")
            .build();

        ServerCapabilities.Prompts promptsCapabilities = ServerCapabilities.Prompts.newBuilder().setListChanged(true).build();
        ServerCapabilities.Resources resourcesCapabilities = ServerCapabilities.Resources.newBuilder().setSubscribe(true).setListChanged(true).build();
        ServerCapabilities.Tools toolsCapabilities = ServerCapabilities.Tools.newBuilder().setListChanged(true).build();

        ServerCapabilities capabilities = ServerCapabilities.newBuilder()
            .setPrompts(promptsCapabilities)
            .setResources(resourcesCapabilities)
            .setTools(toolsCapabilities)
            .build();

        ServerOptions options = ServerOptions.newBuilder()
            .setCapabilities(capabilities)
            .build();

        this.server = new McpServer(implementation, options);
    }

    public void addTool(
            Tool tool,
            Function<CallToolRequest, CompletableFuture<CallToolResult>> handler) {
        
        // Assuming the Java SDK's addTool method takes similar parameters.
        // The schema for inputSchema might need adjustment based on how Java SDK handles it (e.g., Map<String, Object> or JsonNode)
        // For now, passing tool.getInputSchema() directly, assuming it's compatible or will be adapted in ToolBuilder.
        server.addTool(
                tool.getName(),
                tool.getDescription(), // Assuming description is non-null as per Kotlin's !!
                tool.getInputSchema(), // This might need explicit conversion to JsonNode or Map
                handler);
    }

    // Getter for the server if needed by other classes in the same package (like ToolBuilder)
    McpServer getServer() {
        return server;
    }
}
