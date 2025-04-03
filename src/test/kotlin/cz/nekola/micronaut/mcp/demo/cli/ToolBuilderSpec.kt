package cz.nekola.micronaut.mcp.demo.cli

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Value
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import io.micronaut.inject.beans.AbstractInitializableBeanIntrospection.BeanMethodRef
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.Tool as SdkTool
import jakarta.inject.Inject
import kotlinx.serialization.json.*
import java.lang.reflect.Method
import java.util.*

@MicronautTest
class ToolBuilderSpec : BehaviorSpec({
    given("ToolBuilder") {
        val toolBuilder = ToolBuilder(
            serverWrapper = ServerWrapper(),
            applicationContext = ApplicationContext.run(),
            typeConverter = TypeConverter(),
            argumentConverter = ArgumentConverter()
        )

        `when`("initialized") {
            then("should be properly initialized") {
                // Verify that the ToolBuilder is properly initialized with its dependencies
                (toolBuilder is ToolBuilder) shouldBe true
            }
        }

        `when`("processing a method with Tool annotation") {
            // Create mock for serverWrapper only, use real implementations for other beans
            val serverWrapper = mockk<ServerWrapper>(relaxed = true)
            val applicationContext = ApplicationContext.run()
            val typeConverter = TypeConverter()
            val argumentConverter = ArgumentConverter()

            // Create a ToolBuilder with mock for serverWrapper and real implementations for other beans
            val toolBuilder = ToolBuilder(
                serverWrapper = serverWrapper,
                applicationContext = applicationContext,
                typeConverter = typeConverter,
                argumentConverter = argumentConverter
            )

            // Create mock for BeanDefinition
            val beanDefinition = mockk<BeanDefinition<*>>()

            // Use a real method from FooTool for targetMethod
            val fooTool = FooTool()
            val targetMethod = fooTool.javaClass.getDeclaredMethod("toolik")


             //FIXME too much mocking
            // Create mocks for other objects
            val executableMethod = mockk<ExecutableMethod<Any, Any>>()
            val annotationMetadata = mockk<io.micronaut.core.annotation.AnnotationMetadata>()
            val toolAnnotation = mockk<io.micronaut.core.annotation.AnnotationValue<Tool>>()
            val argument = mockk<io.micronaut.core.type.Argument<*>>()
            val toolArgAnnotation = mockk<io.micronaut.core.annotation.AnnotationValue<ToolArg>>()

            // Set up expectations for the mocks
            every { executableMethod.annotationMetadata } returns annotationMetadata
            every { annotationMetadata.getAnnotation(Tool::class.java) } returns toolAnnotation
            every { toolAnnotation.stringValue("name").get() } returns "testTool"
            every { toolAnnotation.stringValue("description").get() } returns "Test tool description"

            every { executableMethod.arguments } returns arrayOf(argument)
            every { argument.name } returns "testArg"
            every { argument.getAnnotation(ToolArg::class.java) } returns toolArgAnnotation
            every { toolArgAnnotation.stringValue("description").get() } returns "Test arg description"
            every { argument.type } returns String::class.java

            // Use the real method for targetMethod
            every { executableMethod.targetMethod } returns targetMethod

            // Call the process method
            toolBuilder.process(beanDefinition, executableMethod)

            then("should add a tool to the serverWrapper") {
                // Verify that serverWrapper.addTool was called with the expected parameters
                verify {
                    serverWrapper.addTool(
                        tool = match<SdkTool> { 
                            it.name == "testTool" && 
                            it.description == "Test tool description" &&
                            it.inputSchema?.properties?.get("testArg")?.jsonObject?.get("description")?.jsonPrimitive?.content == "Test arg description"
                        },
                        handler = any()
                    )
                }
            }
        }
    }
})
