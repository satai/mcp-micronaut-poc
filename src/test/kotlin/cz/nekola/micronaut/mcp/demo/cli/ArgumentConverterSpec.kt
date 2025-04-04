package cz.nekola.micronaut.mcp.demo.cli

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.core.type.Argument
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import kotlinx.serialization.json.*
import kotlin.random.Random

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

            // Helper function to create a CallToolRequest with a parameter
            fun <T> createCallToolRequest(paramName: String, value: T): CallToolRequest {
                val jsonElement = when (value) {
                    is Int -> JsonPrimitive(value)
                    is Long -> JsonPrimitive(value)
                    is Boolean -> JsonPrimitive(value)
                    is Float -> JsonPrimitive(value)
                    is Double -> JsonPrimitive(value)
                    is String -> JsonPrimitive(value)
                    else -> throw IllegalArgumentException("Unsupported type")
                }
                return callToolRequestPrototype.copy(
                    arguments = JsonObject(
                        mapOf(paramName to jsonElement)
                    )
                )
            }

            then("should correctly convert integer types with random values") {
                val paramName = "intParam"
                val intValues = List(10) { Random.nextInt(-1000, 1000) }

                intValues.forEach { intValue ->
                    val callToolRequest = createCallToolRequest(paramName, intValue)

                    // Test both boxed and primitive types
                    val boxedType = Int::class.java
                    val primitiveType = Int::class.javaPrimitiveType!!

                    val boxedArgument = createArgument(paramName, boxedType)
                    val primitiveArgument = createArgument(paramName, primitiveType)

                    argumentConverter.convert(callToolRequest, boxedArgument) shouldBe intValue
                    argumentConverter.convert(callToolRequest, primitiveArgument) shouldBe intValue
                }
            }

            then("should correctly convert long types with random values") {
                val paramName = "longParam"
                val longValues = List(10) { Random.nextLong(-1000, 1000) }

                longValues.forEach { longValue ->
                    val callToolRequest = createCallToolRequest(paramName, longValue)

                    // Test both boxed and primitive types
                    val boxedType = Long::class.java
                    val primitiveType = Long::class.javaPrimitiveType!!

                    val boxedArgument = createArgument(paramName, boxedType)
                    val primitiveArgument = createArgument(paramName, primitiveType)

                    argumentConverter.convert(callToolRequest, boxedArgument) shouldBe longValue
                    argumentConverter.convert(callToolRequest, primitiveArgument) shouldBe longValue
                }
            }

            then("should correctly convert boolean types with all possible values") {
                val paramName = "booleanParam"
                val booleanValues = listOf(true, false)

                booleanValues.forEach { booleanValue ->
                    val callToolRequest = createCallToolRequest(paramName, booleanValue)

                    // Test both boxed and primitive types
                    val boxedType = Boolean::class.java
                    val primitiveType = Boolean::class.javaPrimitiveType!!

                    val boxedArgument = createArgument(paramName, boxedType)
                    val primitiveArgument = createArgument(paramName, primitiveType)

                    argumentConverter.convert(callToolRequest, boxedArgument) shouldBe booleanValue
                    argumentConverter.convert(callToolRequest, primitiveArgument) shouldBe booleanValue
                }
            }

            then("should correctly convert float types with random values") {
                val paramName = "floatParam"
                val floatValues = List(10) { Random.nextFloat() * 2000 - 1000 }

                floatValues.forEach { floatValue ->
                    val callToolRequest = createCallToolRequest(paramName, floatValue)

                    // Test both boxed and primitive types
                    val boxedType = Float::class.java
                    val primitiveType = Float::class.javaPrimitiveType!!

                    val boxedArgument = createArgument(paramName, boxedType)
                    val primitiveArgument = createArgument(paramName, primitiveType)

                    argumentConverter.convert(callToolRequest, boxedArgument) shouldBe floatValue
                    argumentConverter.convert(callToolRequest, primitiveArgument) shouldBe floatValue
                }
            }

            then("should correctly convert double types with random values") {
                val paramName = "doubleParam"
                val doubleValues = List(10) { Random.nextDouble(-1000.0, 1000.0) }

                doubleValues.forEach { doubleValue ->
                    val callToolRequest = createCallToolRequest(paramName, doubleValue)

                    // Test both boxed and primitive types
                    val boxedType = Double::class.java
                    val primitiveType = Double::class.javaPrimitiveType!!

                    val boxedArgument = createArgument(paramName, boxedType)
                    val primitiveArgument = createArgument(paramName, primitiveType)

                    argumentConverter.convert(callToolRequest, boxedArgument) shouldBe doubleValue
                    argumentConverter.convert(callToolRequest, primitiveArgument) shouldBe doubleValue
                }
            }

            then("should correctly convert string types with random values") {
                val paramName = "stringParam"
                val stringValues = List(10) { "test-${Random.nextInt(1000)}" }

                stringValues.forEach { stringValue ->
                    val callToolRequest = createCallToolRequest(paramName, stringValue)
                    val stringArgument = createArgument(paramName, String::class.java)

                    // Note: JsonPrimitive.toString() includes quotes, so we need to check differently
                    argumentConverter.convert(callToolRequest, stringArgument) shouldBe stringValue
                }
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
