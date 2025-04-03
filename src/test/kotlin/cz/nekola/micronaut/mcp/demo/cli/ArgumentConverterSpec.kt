package cz.nekola.micronaut.mcp.demo.cli

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.micronaut.core.type.Argument
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import kotlinx.serialization.json.*

@MicronautTest
class ArgumentConverterSpec : BehaviorSpec({

    given("ArgumentConverter") {
        val argumentConverter = ArgumentConverter()

        `when`("converting JSON elements to Java types") {
            val callToolRequestPrototype = CallToolRequest(
                name = "request_name",
            )

            // Helper function to create an Argument with a specific type
            fun createArgument(name: String, type: Class<*>): Argument<*> {
                return Argument.of(type, name)
            }

            then("should correctly convert integer types") {
                val paramName = "intParam"
                val intValue = 42
                val jsonElement = JsonPrimitive(intValue)
                val callToolRequest = callToolRequestPrototype.copy(
                    arguments = JsonObject(
                        mapOf(paramName to jsonElement)
                    )
                )

                table(
                    headers("Type", "Expected Value"),
                    row(Int::class.java, intValue),
                    row(Int::class.javaPrimitiveType!!, intValue),
                ).forAll { type, expectedValue ->
                    val argument = createArgument(paramName, type)
                    argumentConverter.convert(callToolRequest, argument) shouldBe expectedValue
                }
            }

            then("should correctly convert long types") {
                val paramName = "longParam"
                val longValue = 42L
                val jsonElement = JsonPrimitive(longValue)
                val callToolRequest = callToolRequestPrototype.copy(
                    arguments = JsonObject(
                        mapOf(paramName to jsonElement)
                    )
                )
                table(
                    headers("Type", "Expected Value"),
                    row(Long::class.java, longValue),
                    row(Long::class.javaPrimitiveType!!, longValue),
                ).forAll { type, expectedValue ->
                    val argument = createArgument(paramName, type)
                    argumentConverter.convert(callToolRequest, argument) shouldBe expectedValue
                }
            }

            then("should correctly convert boolean types") {
                val paramName = "booleanParam"
                val booleanValue = true
                val jsonElement = JsonPrimitive(booleanValue)
                val callToolRequest = callToolRequestPrototype.copy(
                    arguments = JsonObject(
                        mapOf(paramName to jsonElement)
                    )
                )
                table(
                    headers("Type", "Expected Value"),
                    row(Boolean::class.java, booleanValue),
                    row(Boolean::class.javaPrimitiveType!!, booleanValue),
                ).forAll { type, expectedValue ->
                    val argument = createArgument(paramName, type)
                    argumentConverter.convert(callToolRequest, argument) shouldBe expectedValue
                }
            }

            then("should correctly convert float types") {
                val paramName = "floatParam"
                val floatValue = 42.0f
                val jsonElement = JsonPrimitive(floatValue)
                val callToolRequest = callToolRequestPrototype.copy(
                    arguments = JsonObject(
                        mapOf(paramName to jsonElement)
                    )
                )
                table(
                    headers("Type", "Expected Value"),
                    row(Float::class.java, floatValue),
                    row(Float::class.javaPrimitiveType!!, floatValue),
                ).forAll { type, expectedValue ->
                    val argument = createArgument(paramName, type)
                    argumentConverter.convert(callToolRequest, argument) shouldBe expectedValue
                }
            }

            then("should correctly convert double types") {
                val paramName = "doubleParam"
                val doubleValue = 42.0
                val jsonElement = JsonPrimitive(doubleValue)
                val callToolRequest = callToolRequestPrototype.copy(
                    arguments = JsonObject(
                        mapOf(paramName to jsonElement)
                    )
                )
                table(
                    headers("Type", "Expected Value"),
                    row(Double::class.java, doubleValue),
                    row(Double::class.javaPrimitiveType!!, doubleValue),
                ).forAll { type, expectedValue ->
                    val argument = createArgument(paramName, type)
                    argumentConverter.convert(callToolRequest, argument) shouldBe expectedValue
                }
            }

            then("should correctly convert string types") {
                val paramName = "stringParam"
                val stringValue = "test"
                val jsonElement = JsonPrimitive(stringValue)
                val callToolRequest = callToolRequestPrototype.copy(
                    arguments = JsonObject(
                        mapOf(paramName to jsonElement)
                    )
                )
                val stringArgument = createArgument(paramName, String::class.java)
                // Note: JsonPrimitive.toString() includes quotes, so we need to check differently
                argumentConverter.convert(callToolRequest, stringArgument) shouldBe jsonElement.toString()
            }

            then("should throw IllegalArgumentException for unsupported types") {
                val paramName = "unsupportedParam"
                val jsonElement = JsonPrimitive("test")
                val callToolRequest = callToolRequestPrototype.copy(
                    arguments = JsonObject(
                        mapOf(paramName to jsonElement)
                    )
                )
                val unsupportedTypes = listOf(
                    Char::class.java,
                    Char::class.javaPrimitiveType,
                    Any::class.java,
                    List::class.java,
                    Map::class.java,
                )

                unsupportedTypes.forEach { type ->
                    val unsupportedArgument = createArgument(paramName, type!!)
                    shouldThrow<IllegalArgumentException> {
                        argumentConverter.convert(callToolRequest, unsupportedArgument)
                    }
                }
            }
        }
    }
})
