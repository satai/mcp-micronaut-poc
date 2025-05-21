package cz.nekola.mcpnaut.demo.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.modelcontextprotocol.sdk.model.CallToolRequest;
import io.modelcontextprotocol.sdk.model.CallToolResult;
import io.modelcontextprotocol.sdk.model.TextContent;
import io.modelcontextprotocol.sdk.model.ToolInput; // Assuming this is the type for input schema
// Or import io.modelcontextprotocol.sdk.model.Tool.Input if it's nested

import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;


@Singleton
public class ToolBuilder implements ExecutableMethodProcessor<Tool> { // Java @Tool annotation

    private final ServerWrapper serverWrapper; // Java ServerWrapper
    private final ApplicationContext applicationContext;
    private final TypeConverter typeConverter; // Java TypeConverter
    private final ArgumentConverter argumentConverter; // Java ArgumentConverter
    private final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

    public ToolBuilder(
            ServerWrapper serverWrapper,
            ApplicationContext applicationContext,
            TypeConverter typeConverter,
            ArgumentConverter argumentConverter) {
        this.serverWrapper = serverWrapper;
        this.applicationContext = applicationContext;
        this.typeConverter = typeConverter;
        this.argumentConverter = argumentConverter;
    }

    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        cz.nekola.mcpnaut.demo.cli.Tool toolAnnotation = method.getAnnotation(cz.nekola.mcpnaut.demo.cli.Tool.class);
        if (toolAnnotation == null) {
            return; // Should not happen if processor is correctly configured
        }

        ObjectNode propertiesNode = nodeFactory.objectNode();
        for (Argument<?> arg : method.getArguments()) {
            ToolArg toolArgAnnotation = arg.getAnnotation(ToolArg.class);
            String description = (toolArgAnnotation != null) ? toolArgAnnotation.description() : "";

            ObjectNode propertyDetails = nodeFactory.objectNode();
            propertyDetails.set("description", nodeFactory.textNode(description));
            
            Map<String, JsonNode> typeInfo = typeConverter.jdkType2McpType(arg.getType());
            typeInfo.forEach(propertyDetails::set);
            
            propertiesNode.set(arg.getName(), propertyDetails);
        }

        // Assuming ToolInput is the correct class and it can be built with an ObjectNode.
        // If it uses a builder: ToolInput.newBuilder().setProperties(propertiesNode).build()
        ToolInput inputSchema = ToolInput.newBuilder().setProperties(propertiesNode).build();


        io.modelcontextprotocol.sdk.model.Tool sdkTool = io.modelcontextprotocol.sdk.model.Tool.newBuilder()
                .setName(toolAnnotation.name())
                .setDescription(toolAnnotation.description())
                .setInputSchema(inputSchema)
                .build();

        serverWrapper.addTool(sdkTool, callHandler(method, beanDefinition));
    }

    private Function<CallToolRequest, CompletableFuture<CallToolResult>> callHandler(
            ExecutableMethod<?, ?> method,
            BeanDefinition<?> beanDefinition) {
        return (callRequest) -> {
            try {
                Object[] args = Arrays.stream(method.getArguments())
                        .map(arg -> argumentConverter.mcpValue2jvmValue(callRequest, arg))
                        .toArray();

                Object bean = applicationContext.getBean(beanDefinition.getBeanType()); // Use getBean(Class)
                Object beanResult = method.invoke(bean, args);

                // Assuming TextContent and CallToolResult are from the Java SDK
                // and CallToolResult can take a List of Content objects.
                TextContent textContent = TextContent.newBuilder().setText(beanResult != null ? beanResult.toString() : "").build();
                CallToolResult callToolResult = CallToolResult.newBuilder().addContent(textContent).build();
                
                return CompletableFuture.completedFuture(callToolResult);
            } catch (Exception e) {
                // Handle exceptions, e.g., by logging and returning a failed CompletableFuture
                // For now, rethrow as a runtime exception or complete exceptionally
                CompletableFuture<CallToolResult> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
        };
    }
}
