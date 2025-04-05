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
                    is Array<*> -> {
                        if (value.isEmpty()) {
                            JsonArray(emptyList())
                        } else {
                            JsonArray(value.map { 
                                when (it) {
                                    is Int -> JsonPrimitive(it)
                                    is Long -> JsonPrimitive(it)
                                    is Boolean -> JsonPrimitive(it)
                                    is Float -> JsonPrimitive(it)
                                    is Double -> JsonPrimitive(it)
                                    is String -> JsonPrimitive(it)
                                    is Array<*> -> {
                                        JsonArray(it.map { innerItem ->
                                            when (innerItem) {
                                                is Int -> JsonPrimitive(innerItem)
                                                is Long -> JsonPrimitive(innerItem)
                                                is Boolean -> JsonPrimitive(innerItem)
                                                is Float -> JsonPrimitive(innerItem)
                                                is Double -> JsonPrimitive(innerItem)
                                                is String -> JsonPrimitive(innerItem)
                                                else -> throw IllegalArgumentException("Unsupported type in inner array")
                                            }
                                        })
                                    }
                                    else -> throw IllegalArgumentException("Unsupported type in array")
                                }
                            })
                        }
                    }
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

                    // Test boxed, primitive, and object types
                    val boxedType = Int::class.java
                    val primitiveType = Int::class.javaPrimitiveType!!
                    val objectType = Int::class.javaObjectType

                    val boxedArgument = createArgument(paramName, boxedType)
                    val primitiveArgument = createArgument(paramName, primitiveType)
                    val objectArgument = createArgument(paramName, objectType)

                    argumentConverter.convert(callToolRequest, boxedArgument) shouldBe intValue
                    argumentConverter.convert(callToolRequest, primitiveArgument) shouldBe intValue
                    argumentConverter.convert(callToolRequest, objectArgument) shouldBe intValue
                }
            }

            then("should correctly convert long types with random values") {
                val paramName = "longParam"
                val longValues = List(10) { Random.nextLong(-1000, 1000) }

                longValues.forEach { longValue ->
                    val callToolRequest = createCallToolRequest(paramName, longValue)

                    // Test boxed, primitive, and object types
                    val boxedType = Long::class.java
                    val primitiveType = Long::class.javaPrimitiveType!!
                    val objectType = Long::class.javaObjectType

                    val boxedArgument = createArgument(paramName, boxedType)
                    val primitiveArgument = createArgument(paramName, primitiveType)
                    val objectArgument = createArgument(paramName, objectType)

                    argumentConverter.convert(callToolRequest, boxedArgument) shouldBe longValue
                    argumentConverter.convert(callToolRequest, primitiveArgument) shouldBe longValue
                    argumentConverter.convert(callToolRequest, objectArgument) shouldBe longValue
                }
            }

            then("should correctly convert boolean types with all possible values") {
                val paramName = "booleanParam"
                val booleanValues = listOf(true, false)

                booleanValues.forEach { booleanValue ->
                    val callToolRequest = createCallToolRequest(paramName, booleanValue)

                    // Test boxed, primitive, and object types
                    val boxedType = Boolean::class.java
                    val primitiveType = Boolean::class.javaPrimitiveType!!
                    val objectType = Boolean::class.javaObjectType

                    val boxedArgument = createArgument(paramName, boxedType)
                    val primitiveArgument = createArgument(paramName, primitiveType)
                    val objectArgument = createArgument(paramName, objectType)

                    argumentConverter.convert(callToolRequest, boxedArgument) shouldBe booleanValue
                    argumentConverter.convert(callToolRequest, primitiveArgument) shouldBe booleanValue
                    argumentConverter.convert(callToolRequest, objectArgument) shouldBe booleanValue
                }
            }

            then("should correctly convert float types with random values") {
                val paramName = "floatParam"
                val floatValues = List(10) { Random.nextFloat() * 2000 - 1000 }

                floatValues.forEach { floatValue ->
                    val callToolRequest = createCallToolRequest(paramName, floatValue)

                    // Test boxed, primitive, and object types
                    val boxedType = Float::class.java
                    val primitiveType = Float::class.javaPrimitiveType!!
                    val objectType = Float::class.javaObjectType

                    val boxedArgument = createArgument(paramName, boxedType)
                    val primitiveArgument = createArgument(paramName, primitiveType)
                    val objectArgument = createArgument(paramName, objectType)

                    argumentConverter.convert(callToolRequest, boxedArgument) shouldBe floatValue
                    argumentConverter.convert(callToolRequest, primitiveArgument) shouldBe floatValue
                    argumentConverter.convert(callToolRequest, objectArgument) shouldBe floatValue
                }
            }

            then("should correctly convert double types with random values") {
                val paramName = "doubleParam"
                val doubleValues = List(10) { Random.nextDouble(-1000.0, 1000.0) }

                doubleValues.forEach { doubleValue ->
                    val callToolRequest = createCallToolRequest(paramName, doubleValue)

                    // Test boxed, primitive, and object types
                    val boxedType = Double::class.java
                    val primitiveType = Double::class.javaPrimitiveType!!
                    val objectType = Double::class.javaObjectType

                    val boxedArgument = createArgument(paramName, boxedType)
                    val primitiveArgument = createArgument(paramName, primitiveType)
                    val objectArgument = createArgument(paramName, objectType)

                    argumentConverter.convert(callToolRequest, boxedArgument) shouldBe doubleValue
                    argumentConverter.convert(callToolRequest, primitiveArgument) shouldBe doubleValue
                    argumentConverter.convert(callToolRequest, objectArgument) shouldBe doubleValue
                }
            }

            then("should correctly convert string types with random values") {
                val paramName = "stringParam"
                val stringValues = List(10) { "test-${Random.nextInt(1000)}" }

                stringValues.forEach { stringValue ->
                    val callToolRequest = createCallToolRequest(paramName, stringValue)
                    val stringArgument = createArgument(paramName, String::class.java)

                    argumentConverter.convert(callToolRequest, stringArgument) shouldBe stringValue
                }
            }

            then("should correctly convert one-dimensional array of integers with random values") {
                val paramName = "intArrayParam"
                val intArrayValues = List(5) { 
                    Array(Random.nextInt(1, 5)) { Random.nextInt(-1000, 1000) }
                }

                intArrayValues.forEach { intArray ->
                    val callToolRequest = createCallToolRequest(paramName, intArray)
                    val arrayType = Array<Int>::class.java
                    val argument = createArgument(paramName, arrayType)

                    val result = argumentConverter.convert(callToolRequest, argument) as Array<*>

                    // Verify the result
                    result.size shouldBe intArray.size
                    for (i in intArray.indices) {
                        result[i] shouldBe intArray[i]
                    }
                }
            }

            then("should correctly convert one-dimensional array of strings with random values") {
                val paramName = "stringArrayParam"
                val stringArrayValues = List(5) { 
                    Array(Random.nextInt(1, 5)) { "test-${Random.nextInt(1000)}" }
                }

                stringArrayValues.forEach { stringArray ->
                    val callToolRequest = createCallToolRequest(paramName, stringArray)
                    val arrayType = Array<String>::class.java
                    val argument = createArgument(paramName, arrayType)

                    val result = argumentConverter.convert(callToolRequest, argument) as Array<*>

                    // Verify the result
                    result.size shouldBe stringArray.size
                    for (i in stringArray.indices) {
                        result[i] shouldBe stringArray[i]
                    }
                }
            }

            then("should correctly convert empty arrays") {
                val paramName = "emptyArrayParam"
                val emptyIntArray = emptyArray<Int>()
                val emptyStringArray = emptyArray<String>()

                // Test empty int array
                val intCallToolRequest = createCallToolRequest(paramName, emptyIntArray)
                val intArrayType = Array<Int>::class.java
                val intArgument = createArgument(paramName, intArrayType)
                val intResult = argumentConverter.convert(intCallToolRequest, intArgument) as Array<*>
                intResult.size shouldBe 0

                // Test empty string array
                val stringCallToolRequest = createCallToolRequest(paramName, emptyStringArray)
                val stringArrayType = Array<String>::class.java
                val stringArgument = createArgument(paramName, stringArrayType)
                val stringResult = argumentConverter.convert(stringCallToolRequest, stringArgument) as Array<*>
                stringResult.size shouldBe 0
            }

            then("should correctly convert multi-dimensional arrays") {
                val paramName = "multiDimArrayParam"

                // Create a 2D array of integers
                val intMatrix = Array(3) { i -> 
                    Array(2) { j -> i * 10 + j }
                }

                val callToolRequest = createCallToolRequest(paramName, intMatrix)
                val arrayType = Array<Array<Int>>::class.java
                val argument = createArgument(paramName, arrayType)

                val result = argumentConverter.convert(callToolRequest, argument) as Array<*>

                // Verify the result
                result.size shouldBe intMatrix.size
                for (i in intMatrix.indices) {
                    val innerResult = result[i] as Array<*>
                    val innerExpected = intMatrix[i]
                    innerResult.size shouldBe innerExpected.size
                    for (j in innerExpected.indices) {
                        innerResult[j] shouldBe innerExpected[j]
                    }
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
