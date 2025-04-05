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
    private val typeConverter: TypeConverter,
    private val argumentConverter: ArgumentConverter,
): ExecutableMethodProcessor<Tool> {
    override fun process(beanDefinition: BeanDefinition<*>?, method: ExecutableMethod<*, *>?) {

        val annotation = method!!.annotationMetadata.getAnnotation(Tool::class.java)
        val inputSchema = SdkTool.Input(
            properties = JsonObject(
                method.arguments.associate {
                    val description = it.getAnnotation(ToolArg::class.java).stringValue("description").get()
                    it.name to JsonObject(
                        mapOf(
                            "description" to JsonPrimitive(description)
                        ) + typeConverter.jdkType2McpType(it.type)
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
            handler = callHandler(method, beanDefinition)
        )
    }

    private fun callHandler(
        method: ExecutableMethod<*, *>,
        beanDefinition: BeanDefinition<*>?,
    ): suspend (CallToolRequest) -> CallToolResult =
        { call ->
            val args: Array<Any?> = method.arguments.map { parameter -> argumentConverter.mcpValue2jvmValue(call, parameter) }.toTypedArray()
            val bean = applicationContext.getBean(beanDefinition)
            val beanResult = method.targetMethod.invoke(bean, *args)
            CallToolResult(listOf(TextContent(beanResult.toString())))
        }


}
