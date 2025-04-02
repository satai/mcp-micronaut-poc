package cz.nekola.micronaut.mcp.demo.cli

import io.micronaut.context.ApplicationContext
import io.micronaut.context.processor.ExecutableMethodProcessor
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.Tool as SdkTool
import jakarta.inject.Singleton
import kotlinx.serialization.json.*

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