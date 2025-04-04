package cz.nekola.micronaut.mcp.demo.cli

import io.kotest.matchers.string.shouldContain
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.should
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.ClientOptions
import io.modelcontextprotocol.kotlin.sdk.client.StdioClientTransport
import kotlinx.coroutines.runBlocking
import kotlinx.io.Source
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.ByteArrayOutputStream
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

        val transport = StdioClientTransport(
            input   = clientIn.asSource().buffered(),
            output = clientToServer.asSink().buffered(),
        )

        `when`("using Client with Std in out communication to list tools") {

            then("should be able to list tools") {
                runBlocking {
                    try {
                        client.connect(transport = transport)
                        val tools = client.listTools()
                        tools!!.tools.map{it.name}.shouldContain("footoolik")
                        client.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        throw e
                    }
                }
            }

            // Restore original System.in and System.out
            System.setIn(originalIn)
            System.setOut(originalOut)

            // Interrupt the command thread
            commandThread.interrupt()
        }

        ctx.close()
    }
})
