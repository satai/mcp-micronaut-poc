package cz.nekola.mcpnaut.demo.cli

import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import jakarta.inject.Singleton

@Singleton
class ServerWrapper(

) {
    internal val server: Server = Server(
        Implementation(
            name = "mcp-kotlin test server",
            version = "0.1.0"
        ),
        ServerOptions(
            capabilities = ServerCapabilities(
                prompts = ServerCapabilities.Prompts(listChanged = true),
                resources = ServerCapabilities.Resources(subscribe = true, listChanged = true),
                tools = ServerCapabilities.Tools(listChanged = true),
            )
        )
    )

    fun addTool(
        tool: io.modelcontextprotocol.kotlin.sdk.Tool,
        handler: suspend (CallToolRequest) -> CallToolResult,
    ) {
        server.addTool(
            name = tool.name,
            description = tool.description!!,
            inputSchema = tool.inputSchema,
            handler = handler,
        )

    }
}