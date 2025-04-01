package cz.nekola.micronaut.mcp.demo.cli

import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.Tool as SdkTool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(
    name = "micronaut-mcp-cli", description = ["..."],
    mixinStandardHelpOptions = true
)
class MicronautMcpCliCommand(
) : Runnable {

    @Option(names = ["-v", "--verbose"], description = ["..."])
    private var verbose: Boolean = false

    @Inject
    var applicationContext: ApplicationContext? =  null

    override fun run() {
        // business logic here
        if (verbose) {
            println("Hi!")
        }

        val server = Server(
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
        server.addTool(
            name = "foo",
            description = "A test tool, fooing mostly",
            inputSchema = SdkTool.Input(
                properties = JsonObject(mapOf(
                    "par" to JsonObject(
                        mapOf("type" to JsonPrimitive("number"))
                    ),
                ))
            )
        ) { request ->
            CallToolResult(
                content = listOf(TextContent("Hello, ${request.arguments}!"))
            )
        }
        val transport = StdioServerTransport(
            inputStream = System.`in`.asSource().buffered(),
            outputStream = System.out.asSink().buffered()
        )

        runBlocking {
            println("${applicationContext!!.getBean(ToolBuilder::class.java)}")
            println("${applicationContext!!.getBean(cz.nekola.micronaut.mcp.demo.cli.FooTool::class.java)}")
            server.connect(transport)
            val done = Job()
            server.onClose {
                done.complete()
            }
            done.join()
            println("Server closed")
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            PicocliRunner.run(MicronautMcpCliCommand::class.java, *args)
        }
    }
}
