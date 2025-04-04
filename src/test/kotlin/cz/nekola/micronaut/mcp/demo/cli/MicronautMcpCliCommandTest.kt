package cz.nekola.micronaut.mcp.demo.cli

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.ClientOptions
import io.modelcontextprotocol.kotlin.sdk.client.StdioClientTransport
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream
import kotlin.concurrent.thread

class MicronautMcpCliCommandTest : BehaviorSpec({

    given("micronaut-mcp-cli") {
        val ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)

        val client = Client(
            clientInfo =  Implementation(
                name = "cz.nekola.micronaut.mcp.demo.cli",
                version = "0.0.1"
            ),
            options = ClientOptions(

            ),
        )

        // Set up pipes for communication
        val clientToServer = PipedOutputStream()
        val serverToClient = PipedOutputStream()
        val serverIn = PipedInputStream(clientToServer)
        val clientIn = PipedInputStream(serverToClient)

        // Save original System.in and System.out
        val originalIn = System.`in`
        val originalOut = System.out

        // Redirect System.in and System.out for the command
        System.setIn(serverIn)
        System.setOut(PrintStream(serverToClient))

        // Start the command in a separate thread
        val commandThread = thread {
            val args = arrayOf<String>()
            PicocliRunner.run(MicronautMcpCliCommand::class.java, ctx, *args)
        }

        `when`("using Client with Std in out communication to list tools") {

            val transport = StdioClientTransport(
                input   = clientIn.asSource().buffered(),
                output = clientToServer.asSink().buffered(),
            )

            client.connect(transport = transport)

            then("should be able to list tools") {
                runBlocking {
                    try {
                        val tools = client.listTools()
                        tools!!.tools.map{it.name}.shouldContain("footoolik")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        throw e
                    }
                }
            }

            then("should be able to use tools") {
                runBlocking {
                    try {
                        client.callTool(
                            CallToolRequest(
                                name = "footoolai3",
                                arguments = JsonObject(mapOf(
                                    "arg1" to JsonPrimitive(1234)
                                ))
                            )
                        )!!.content shouldBe listOf(TextContent("toolik 1234"))
                        client.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        throw e
                    }
                }
            }

            client.close()

            // Restore original System.in and System.out
            System.setIn(originalIn)
            System.setOut(originalOut)

            // Interrupt the command thread
            commandThread.interrupt()
        }

        ctx.close()
    }
})
