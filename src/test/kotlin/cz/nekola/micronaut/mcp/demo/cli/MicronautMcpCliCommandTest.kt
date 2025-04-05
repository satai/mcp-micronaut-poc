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
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

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
                    } catch (e: Exception) {
                        e.printStackTrace()
                        throw e
                    }
                }
            }

            then("should be able to use tools with multiple args") {
                runBlocking {
                    try {
                        client.callTool(
                            CallToolRequest(
                                name = "footoolai2",
                                arguments = JsonObject(mapOf(
                                    "arg11" to JsonPrimitive(1234),
                                    "arg22" to JsonPrimitive("arg2value")
                                ))
                            )
                        )!!.content shouldBe listOf(TextContent("""toolik 1234 arg2value"""))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        throw e
                    }
                }
            }

            then("should be able to call all variants of MultiTypeToolKotlin tools") {
                // Create a table with tool name, parameter name, parameter value, and expected response
                table(
                    headers("Tool Name", "Parameter Name", "Parameter Value", "Expected Response"),
                    row("singleParamTool_int", "param1", JsonPrimitive(42), "MultiTypeToolKotlin_int answer 42"),
                    row("singleParamTool_Integer", "param1", JsonPrimitive(123), "MultiTypeToolKotlin_Integer answer 123"),
                    row("singleParamTool_bool", "param1", JsonPrimitive(true), "MultiTypeToolKotlin_Bool answer true"),
                    row("singleParamTool_long", "param1", JsonPrimitive(9876543210L), "MultiTypeToolKotlin_Long answer 9876543210"),
                    row("singleParamTool_float", "param1", JsonPrimitive(3.14f), "MultiTypeToolKotlin_Float answer 3.14"),
                    row("singleParamTool_double", "param1", JsonPrimitive(2.71828), "MultiTypeToolKotlin_Double answer 2.71828"),
                    row("singleParamTool_string", "param1", JsonPrimitive("hello"), "MultiTypeToolKotlin_String answer hello"),
                    row("singleParamTool_array_of_strings", "param1", JsonArray(listOf(JsonPrimitive("eins"))), "MultiTypeToolKotlin_Array_of_Strings answer eins"),
                    row("singleParamTool_array_of_strings", "param1", JsonArray(listOf(JsonPrimitive("eins"), JsonPrimitive("zwei"))), "MultiTypeToolKotlin_Array_of_Strings answer eins, zwei"),
                    row("singleParamTool_array_of_ints", "param1", JsonArray(listOf(JsonPrimitive(1), JsonPrimitive(2))), "MultiTypeToolKotlin_Array_of_Ints answer 1, 2"),
                    row("singleParamTool_array_of_ints", "param1", JsonArray(listOf()), "MultiTypeToolKotlin_Array_of_Ints answer "),
                    row("singleParamTool_array_of_array_of_ints", "param1", JsonArray(listOf(JsonArray(listOf(JsonPrimitive(11), JsonPrimitive(12))), JsonArray(listOf(JsonPrimitive(2))))), "MultiTypeToolKotlin_Array_of_Array_of_Ints answer [[11, 12], [2]]"),
                    row("singleParamTool_int_java", "param1", JsonPrimitive(42), "MultiTypeToolJava_int answer 42"),
                    row("singleParamTool_Integer_java", "param1", JsonPrimitive(123), "MultiTypeToolJava_Integer answer 123"),
                    row("singleParamTool_bool_java", "param1", JsonPrimitive(true), "MultiTypeToolJava_Bool answer true"),
                    row("singleParamTool_long_java", "param1", JsonPrimitive(9876543210L), "MultiTypeToolJava_Long answer 9876543210"),
                    row("singleParamTool_float_java", "param1", JsonPrimitive(3.14f), "MultiTypeToolJava_Float answer 3.14"),
                    row("singleParamTool_double_java", "param1", JsonPrimitive(2.71828), "MultiTypeToolJava_Double answer 2.71828"),
                    row("singleParamTool_string_java", "param1", JsonPrimitive("hello"), "MultiTypeToolJava_String answer hello"),
                    row("singleParamTool_Boolean_java", "param1", JsonPrimitive(false), "MultiTypeToolJava_Boolean answer false"),
                    row("singleParamTool_array_of_strings_java", "param1", JsonArray(listOf(JsonPrimitive("eins"), JsonPrimitive("zwei"))), "MultiTypeToolJava_ArrayOfStrings answer [eins, zwei]"),
//                    row("singleParamTool_array_of_ints_java", "param1", JsonArray(listOf(JsonPrimitive(1), JsonPrimitive(2))), "MultiTypeToolJava_ArrayOfInts answer [1, 2]"),
//                    row("singleParamTool_array_of_arrays_of_ints_java", "param1", JsonArray(listOf(JsonArray(listOf(JsonPrimitive(11), JsonPrimitive(12))), JsonArray(listOf(JsonPrimitive(2))))), "MultiTypeToolJava_ArrayOfArrayOfInts answer [[11, 12], [2]]"),
                    row("singleParamTool_array_of_arrays_of_strings_java", "param1", JsonArray(listOf(JsonArray(listOf(JsonPrimitive(11), JsonPrimitive(12))), JsonArray(listOf(JsonPrimitive(2))))), "MultiTypeToolJava_ArrayOfArrayyOfStrings answer [[11, 12], [2]]"),
                    row("singleParamTool_int_groovy", "param1", JsonPrimitive(42), "MultiTypeToolGroovy_int answer 42"),
                    row("singleParamTool_Integer_groovy", "param1", JsonPrimitive(123), "MultiTypeToolGroovy_Integer answer 123"),
                    row("singleParamTool_bool_groovy", "param1", JsonPrimitive(true), "MultiTypeToolGroovy_Bool answer true"),
                    row("singleParamTool_long_groovy", "param1", JsonPrimitive(9876543210L), "MultiTypeToolGroovy_Long answer 9876543210"),
                    row("singleParamTool_float_groovy", "param1", JsonPrimitive(3.14f), "MultiTypeToolGroovy_Float answer 3.14"),
                    row("singleParamTool_double_groovy", "param1", JsonPrimitive(2.71828), "MultiTypeToolGroovy_Double answer 2.71828"),
                    row("singleParamTool_string_groovy", "param1", JsonPrimitive("hello"), "MultiTypeToolGroovy_String answer hello"),
                ).forAll { toolName, paramName, paramValue, expectedResponse ->
                    runBlocking {
                        try {
                            val response = client.callTool(
                                CallToolRequest(
                                    name = toolName,
                                    arguments = JsonObject(mapOf(
                                        paramName to paramValue
                                    ))
                                )
                            )
                            response!!.content shouldBe listOf(TextContent(expectedResponse))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            throw e
                        }
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

    // TODO tests for error states
})
