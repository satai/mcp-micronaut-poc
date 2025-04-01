package cz.nekola.micronaut.mcp.demo.cli

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Executable
import io.micronaut.context.processor.ExecutableMethodProcessor
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.Tool as SdkTool
import jakarta.inject.Singleton
import kotlinx.serialization.json.*

@MustBeDocumented // FIXME
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Executable(processOnStartup = true)
annotation class Tool(
    val name: String,
    val description: String,
)

@MustBeDocumented // FIXME
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
@Executable(processOnStartup = true)
annotation class ToolArg(
    val name: String, // TODO don't require, default to arg name
    val description: String
)

@Singleton
class FooTool() {
    @Tool(
        name = "footoolik",
        description = "Toolik that Foos a lot"
    )
    fun toolik() = "toolik"

    @Tool(
        name = "footoolai2",
        description = "Toolik2 that Foos a lot"
    )
    fun toolika2(
        @ToolArg(name="arg1", description="Arg 1 desc") arg11: Int,
        @ToolArg("arg1", "Arg 2 desc") arg22: String
    ): String {
        return "toolik $arg11 $arg22"
    }


    @Tool(
        name = "footoolai3",
        description = "Toolik3 that Foos a lot"
    )
    fun toolika3(
        @ToolArg(name="arg1", description="Arg 1 desc") arg1: Int,
    ): String {
        return "toolik $arg1"
    }
}

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

@Singleton
class ToolBuilder(
    private val serverWrapper: ServerWrapper,
    private val applicationContext: ApplicationContext,
): ExecutableMethodProcessor<Tool> {
    override fun process(beanDefinition: BeanDefinition<*>?, method: ExecutableMethod<*, *>?) {

        val annotation = method!!.annotationMetadata.getAnnotation(Tool::class.java)
        val inputSchema = io.modelcontextprotocol.kotlin.sdk.Tool.Input(
            properties = JsonObject(
                method.arguments.associate {
                    val description = it.getAnnotation(ToolArg::class.java).stringValue("description").get()
                    it.name to JsonObject(
                        mapOf(
                            "type" to JsonPrimitive(if (it.type == String::class.java) "string" else "number"), //TODO
                            "description" to JsonPrimitive(description)
                        )
                    )
                }
            )
        )

        serverWrapper.addTool(
            tool = SdkTool(
                name = annotation.stringValue("name").get(),
                description = annotation.stringValue("description").get(),
                inputSchema = inputSchema
            ),
            handler = {
                call ->
                    val toTypedArray: Array<Any> = method.arguments.map { arg ->
                        val element: JsonElement = call.arguments[arg.name]!!
                        if (arg.type == String::class.java) element.jsonPrimitive.toString() else element.jsonPrimitive.intOrNull!!
                    }.toTypedArray()
                val bean = applicationContext.getBean(beanDefinition)
                val beanResult = method.targetMethod.invoke(bean, *toTypedArray)
                CallToolResult(listOf(TextContent(beanResult.toString())))

            }
        )
    }
}

