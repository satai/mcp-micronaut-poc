package cz.nekola.mcpnaut.demo.cli

import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered

import picocli.CommandLine.Command

@Command(
    name = "micronaut-mcp-cli", description = ["..."],
    mixinStandardHelpOptions = true
)
class MicronautMcpCliCommand(
) : Runnable {

    @Inject
    var applicationContext: ApplicationContext? =  null

    override fun run() {
        val transport = StdioServerTransport(
            inputStream = System.`in`.asSource().buffered(),
            outputStream = System.out.asSink().buffered()
        )

        runBlocking {
            val serverWrapper = applicationContext!!.getBean(ServerWrapper::class.java)
            serverWrapper.server.connect(transport)
            val done = Job()
            serverWrapper.server.onClose {
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
