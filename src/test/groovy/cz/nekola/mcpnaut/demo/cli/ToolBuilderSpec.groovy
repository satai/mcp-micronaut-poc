package cz.nekola.mcpnaut.demo.cli

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.micronaut.context.ApplicationContext // Keep if ToolBuilder explicitly uses it beyond getBean
import io.micronaut.core.annotation.AnnotationMetadata
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.core.type.Argument
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.modelcontextprotocol.sdk.model.CallToolRequest
import io.modelcontextprotocol.sdk.model.CallToolResult
import io.modelcontextprotocol.sdk.model.ToolInput 
import jakarta.inject.Inject
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.function.Function
import java.util.Optional // Required for Optional.of

@MicronautTest
class ToolBuilderSpec extends Specification {

    @Inject
    ToolBuilder toolBuilder // The class under test, Micronaut will inject its real dependencies

    // This mock will be injected into ToolBuilder by Micronaut's test context
    @Inject
    ServerWrapper serverWrapper = Mock(ServerWrapper) 

    // TypeConverter and ArgumentConverter are real beans injected into ToolBuilder by Micronaut.
    // ApplicationContext is also injected into ToolBuilder.

    def "processing a method with @Tool annotation should add a correctly configured tool to ServerWrapper"() {
        given: "A mocked bean definition and executable method"
        BeanDefinition beanDefinitionMock = Mock()
        ExecutableMethod executableMethodMock = Mock()
        AnnotationMetadata methodAnnotationMetadataMock = Mock() // Mock for method's annotations
        AnnotationValue<Tool> toolAnnotationValueMock = Mock() // Mock for @Tool annotation itself
        
        // Mocking arguments of the executable method
        Argument<?> stringArgMock = Mock(Argument) // Mocking Argument itself
        AnnotationMetadata argAnnotationMetadataMock = Mock() // Mock for argument's annotations
        AnnotationValue<ToolArg> toolArgAnnotationValueMock = Mock() // Mock for @ToolArg annotation

        // Setup behavior for mocks related to @Tool annotation on the method
        executableMethodMock.getAnnotationMetadata() >> methodAnnotationMetadataMock
        // The ToolBuilder specifically calls method.getAnnotation(Tool.class)
        executableMethodMock.getAnnotation(Tool.class) >> toolAnnotationValueMock
        toolAnnotationValueMock.stringValue("name") >> Optional.of("testTool")
        toolAnnotationValueMock.stringValue("description") >> Optional.of("Test tool description")

        // Setup for method arguments
        executableMethodMock.getArguments() >> ([stringArgMock] as Argument<?>[])
        
        stringArgMock.getName() >> "testArg"
        stringArgMock.getType() >> String.class // Crucial for TypeConverter
        
        // Setup for @ToolArg annotation on the argument
        // ToolBuilder uses arg.getAnnotation(ToolArg.class)
        stringArgMock.getAnnotation(ToolArg.class) >> toolArgAnnotationValueMock 
        toolArgAnnotationValueMock.stringValue("description") >> Optional.of("Test arg description")

        // ToolBuilder uses applicationContext.getBean(beanDefinition.getBeanType())
        beanDefinitionMock.getBeanType() >> Object.class // Or a specific type if known/needed by getBean

        when: "ToolBuilder processes the method"
        toolBuilder.process(beanDefinitionMock, executableMethodMock)

        then: "ServerWrapper.addTool is called with the expected Tool configuration"
        1 * serverWrapper.addTool(
            { io.modelcontextprotocol.sdk.model.Tool toolSdkArg -> // Renamed to avoid clash with Tool.class
                // Verify tool properties
                toolSdkArg.getName() == "testTool"
                toolSdkArg.getDescription() == "Test tool description"
                
                // Verify inputSchema
                ToolInput inputSchema = toolSdkArg.getInputSchema()
                inputSchema != null
                JsonNode properties = inputSchema.getProperties() // Assuming getProperties() returns JsonNode
                properties instanceof ObjectNode
                ObjectNode propertiesObjNode = (ObjectNode) properties
                
                propertiesObjNode.has("testArg")
                JsonNode testArgSchema = propertiesObjNode.get("testArg")
                testArgSchema instanceof ObjectNode
                ObjectNode testArgObjNode = (ObjectNode) testArgSchema
                
                // Verify description from @ToolArg
                testArgObjNode.get("description").asText() == "Test arg description"
                // Verify type information from TypeConverter (for String.class)
                testArgObjNode.get("type").asText() == "String" 
                true // Closure must return boolean for Spock interaction matching
            },
            _ as Function<CallToolRequest, CompletableFuture<CallToolResult>> // Match handler type
        )
    }
}
