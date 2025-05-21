package cz.nekola.mcpnaut.demo.cli

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ArrayNode
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.modelcontextprotocol.sdk.client.McpClient
import io.modelcontextprotocol.sdk.client.ClientOptions
import io.modelcontextprotocol.sdk.transport.StdioClientTransport
import io.modelcontextprotocol.sdk.model.CallToolRequest
import io.modelcontextprotocol.sdk.model.Implementation
import io.modelcontextprotocol.sdk.model.TextContent
import spock.lang.Specification
import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.Unroll

import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream
import java.util.concurrent.CompletableFuture // For .get()

@Stepwise
class MicronautMcpCliCommandSpec extends Specification {

    ApplicationContext ctx
    McpClient client

    PipedOutputStream clientToServerPipeOutput
    PipedInputStream serverInPipeInput // CLI System.in

    PipedOutputStream serverToClientPipeOutput // CLI System.out
    PipedInputStream clientFromServerPipeInput

    InputStream originalSystemIn
    PrintStream originalSystemOut

    Thread commandThread
    @Shared JsonNodeFactory nodeFactory = JsonNodeFactory.instance

    def setup() {
        originalSystemIn = System.in
        originalSystemOut = System.out

        clientToServerPipeOutput = new PipedOutputStream()
        serverInPipeInput = new PipedInputStream(clientToServerPipeOutput, 8192) // Increased buffer size

        serverToClientPipeOutput = new PipedOutputStream()
        clientFromServerPipeInput = new PipedInputStream(serverToClientPipeOutput, 8192) // Increased buffer size

        System.setIn(serverInPipeInput)
        System.setOut(new PrintStream(serverToClientPipeOutput, true)) // true for autoFlush

        ctx = ApplicationContext.builder(Environment.CLI, Environment.TEST).start()

        // Ensure FooTool and MultiTypeToolGroovy are loaded by referencing them
        // This is important because ToolBuilder processes beans on startup.
        ctx.getBean(FooTool.class)
        ctx.getBean(MultiTypeToolGroovy.class)

        commandThread = Thread.start {
            try {
                PicocliRunner.run(MicronautMcpCliCommand.class, ctx, [] as String[])
            } catch (Exception e) {
                // This might be useful for debugging if PicocliRunner fails
                System.err.println("Error in commandThread: " + e.getMessage())
                e.printStackTrace(System.err)
            }
        }

        Implementation clientInfo = Implementation.newBuilder()
            .setName("test-client")
            .setVersion("1.0.0")
            .build()
        
        StdioClientTransport transport = new StdioClientTransport(clientFromServerPipeInput, clientToServerPipeOutput)
        
        client = new McpClient(clientInfo, ClientOptions.newBuilder().build(), transport)
        
        // McpClient.connect() is blocking, so run it in a separate thread or ensure it non-blocking.
        // The Java SDK client.connect() typically starts its own reader thread and is non-blocking.
        client.connect() 
    }

    def cleanup() {
        client?.close() // Close client connection

        // Attempt to close streams gracefully
        try { clientToServerPipeOutput?.close() } catch (IOException e) { /* ignore */ }
        try { serverInPipeInput?.close() } catch (IOException e) { /* ignore */ }
        try { serverToClientPipeOutput?.close() } catch (IOException e) { /* ignore */ }
        try { clientFromServerPipeInput?.close() } catch (IOException e) { /* ignore */ }

        System.setIn(originalSystemIn)
        System.setOut(originalSystemOut)

        commandThread?.interrupt()
        try {
            commandThread?.join(5000) // Wait for thread to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt()
        }

        ctx?.close()
    }

    def "should be able to list tools including footoolik"() {
        when: "listing tools"
        // Adding a short delay to ensure server is up and ready
        // This can be flaky, proper readiness check would be better
        sleep(1000) // wait 1 second for server to initialize tools
        def listedToolsFuture = client.listTools()
        def listedTools = listedToolsFuture.get() // Using .get() for simplicity

        then: "the list of tools is not null and contains 'footoolik'"
        listedTools != null
        listedTools.getTools().collect { it.getName() }.contains("footoolik")
        listedTools.getTools().collect { it.getName() }.contains("singleParamTool_int_groovy") // Verify another tool
    }

    def "should be able to call footoolai3"() {
        given:
        ObjectNode args = nodeFactory.objectNode()
        args.put("arg1", 1234)
        
        CallToolRequest request = CallToolRequest.newBuilder()
            .setName("footoolai3") // From FooTool.groovy
            .setArguments(args)
            .build()

        when:
        def result = client.callTool(request).get()

        then:
        result != null
        result.getContentList().size() == 1
        result.getContentList().get(0) instanceof TextContent
        ((TextContent)result.getContentList().get(0)).getText() == "toolik 1234"
    }
    
    def "should be able to call footoolai2"() {
        given:
        ObjectNode args = nodeFactory.objectNode()
        args.put("arg11", 1234)
        args.put("arg22", "arg2value")

        CallToolRequest request = CallToolRequest.newBuilder()
            .setName("footoolai2") // From FooTool.groovy
            .setArguments(args)
            .build()
        when:
        def result = client.callTool(request).get()

        then:
        result != null
        result.getContentList().size() == 1
        result.getContentList().get(0) instanceof TextContent
        ((TextContent)result.getContentList().get(0)).getText() == "toolik 1234 arg2value"
    }

    @Unroll("call MultiTypeToolGroovy: #toolName with #paramName = #paramValue (#jsonValue.getNodeType()) -> #expectedResponse")
    def "should correctly call various MultiTypeToolGroovy tools"() {
        given:
        ObjectNode args = nodeFactory.objectNode()
        args.set(paramName, jsonValue)

        CallToolRequest request = CallToolRequest.newBuilder()
            .setName(toolName)
            .setArguments(args)
            .build()

        when:
        def result = client.callTool(request).get()

        then:
        result != null
        result.getContentList().size() == 1
        result.getContentList().get(0) instanceof TextContent
        ((TextContent)result.getContentList().get(0)).getText() == expectedResponse

        where:
        toolName                                      | paramName | jsonValue                                                                                 | expectedResponse
        // Primitives & Strings
        "singleParamTool_int_groovy"                  | "param1"  | nodeFactory.numberNode(42)                                                                | "MultiTypeToolGroovy_int answer 42"
        "singleParamTool_Integer_groovy"              | "param1"  | nodeFactory.numberNode(123)                                                               | "MultiTypeToolGroovy_Integer answer 123"
        "singleParamTool_boolean_groovy"              | "param1"  | nodeFactory.booleanNode(true)                                                             | "MultiTypeToolGroovy_boolean answer true" // Adjusted from "Bool" to "boolean"
        "singleParamTool_long_groovy"                 | "param1"  | nodeFactory.numberNode(9876543210L)                                                       | "MultiTypeToolGroovy_long answer 9876543210"  // Adjusted from "Long" to "long"
        "singleParamTool_float_groovy"                | "param1"  | nodeFactory.numberNode(3.14f)                                                             | "MultiTypeToolGroovy_float answer 3.14"   // Adjusted from "Float" to "float"
        "singleParamTool_double_groovy"               | "param1"  | nodeFactory.numberNode(2.71828)                                                           | "MultiTypeToolGroovy_double answer 2.71828" // Adjusted from "Double" to "double"
        "singleParamTool_string_groovy"               | "param1"  | nodeFactory.textNode("hello")                                                             | "MultiTypeToolGroovy_String answer hello"
        
        // 1D Arrays
        "singleParamTool_string_array_groovy"         | "param1"  | nodeFactory.arrayNode().add("eins").add("zwei")                                           | "MultiTypeToolGroovy_string_array answer [eins, zwei]"
        "singleParamTool_int_array_groovy"            | "param1"  | nodeFactory.arrayNode().add(1).add(2)                                                     | "MultiTypeToolGroovy_int_array answer [1, 2]"
        "singleParamTool_int_array_groovy"            | "param1"  | nodeFactory.arrayNode()                                                                   | "MultiTypeToolGroovy_int_array answer []"
        "singleParamTool_boolean_array_groovy"        | "param1"  | nodeFactory.arrayNode().add(true).add(false)                                              | "MultiTypeToolGroovy_boolean_array answer [true, false]"
        
        // 2D Arrays
        "singleParamTool_int_array_array_groovy"      | "param1"  | nodeFactory.arrayNode().add(nodeFactory.arrayNode().add(11).add(12)).add(nodeFactory.arrayNode().add(2)) | "MultiTypeToolGroovy_int_array_array answer [[11, 12], [2]]"
        "singleParamTool_string_array_array_groovy"   | "param1"  | nodeFactory.arrayNode().add(nodeFactory.arrayNode().add("r1c1").add("r1c2")).add(nodeFactory.arrayNode().add("r2c1")) | "MultiTypeToolGroovy_string_array_array answer [[r1c1, r1c2], [r2c1]]"
        "singleParamTool_boolean_array_array_groovy"  | "param1"  | nodeFactory.arrayNode().add(nodeFactory.arrayNode().add(true).add(false)).add(nodeFactory.arrayNode().add(true)) | "MultiTypeToolGroovy_boolean_array_array answer [[true, false], [true]]"
        "singleParamTool_int_array_array_groovy"      | "param1"  | nodeFactory.arrayNode()                                                                    | "MultiTypeToolGroovy_int_array_array answer []" // Empty 2D array
    }
}
