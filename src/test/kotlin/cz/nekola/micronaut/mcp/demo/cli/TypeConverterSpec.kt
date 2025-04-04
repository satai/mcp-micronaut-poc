package cz.nekola.micronaut.mcp.demo.cli

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import kotlinx.serialization.json.JsonPrimitive

@MicronautTest
class TypeConverterSpec : BehaviorSpec({

    given("TypeConverter") {
        val typeConverter = TypeConverter()

        `when`("converting JDK types to MCP types") {
            then("should correctly convert String type") {
                typeConverter.jdkType2McpType(String::class.java)shouldBe mapOf("type" to JsonPrimitive("String"))
            }

            then("should correctly convert integer types") {
                table(
                    headers("JDK Type", "Expected MCP Type"),
                    row(Int::class.javaPrimitiveType, JsonPrimitive("integer")),
                    row(Integer::class.java, JsonPrimitive("integer")),
                    row(Long::class.javaPrimitiveType, JsonPrimitive("integer")),
                    row(Long::class.java, JsonPrimitive("integer")),
                ).forAll { jdkType, expectedMcpType ->
                    typeConverter.jdkType2McpType(jdkType!!) shouldBe mapOf("type" to expectedMcpType)
                }
            }

            then("should correctly convert floating-point types") {
                table(
                    headers("JDK Type", "Expected MCP Type"),
                    row(Double::class.javaPrimitiveType, JsonPrimitive("number")),
                    row(Double::class.java, JsonPrimitive("number")),
                    row(Float::class.javaPrimitiveType, JsonPrimitive("number")),
                    row(Float::class.java, JsonPrimitive("number")),
                ).forAll { jdkType, expectedMcpType ->
                    typeConverter.jdkType2McpType(jdkType!!) shouldBe mapOf("type" to expectedMcpType)
                }
            }

            then("should correctly convert boolean types") {
                table(
                    headers("JDK Type", "Expected MCP Type"),
                    row(Boolean::class.javaPrimitiveType, JsonPrimitive("boolean")),
                    row(Boolean::class.java, JsonPrimitive("boolean")),
                ).forAll { jdkType, expectedMcpType ->
                    typeConverter.jdkType2McpType(jdkType!!) shouldBe mapOf("type" to expectedMcpType)
                }
            }

            then("should correctly convert one-dimensional array types") {
                table(
                    headers("JDK Array Type", "Expected MCP Type"),
                    row(Array<Int>::class.java, mapOf(
                        "type" to JsonPrimitive("array"),
                        "items" to JsonPrimitive("integer")
                    )),
                    row(Array<String>::class.java, mapOf(
                        "type" to JsonPrimitive("array"),
                        "items" to JsonPrimitive("String")
                    )),
                    row(Array<Boolean>::class.java, mapOf(
                        "type" to JsonPrimitive("array"),
                        "items" to JsonPrimitive("boolean")
                    )),
                    row(Array<Double>::class.java, mapOf(
                        "type" to JsonPrimitive("array"),
                        "items" to JsonPrimitive("number")
                    )),
                ).forAll { jdkType, expectedMcpType ->
                    typeConverter.jdkType2McpType(jdkType) shouldBe expectedMcpType
                }
            }

            then("should correctly convert multi-dimensional array types") {
                // Test 2D array of integers
                val intMatrix2DType = Array<Array<Int>>::class.java
                val expectedIntMatrix2DType = mapOf(
                    "type" to JsonPrimitive("array"),
                    "items" to mapOf(
                        "type" to JsonPrimitive("array"),
                        "items" to JsonPrimitive("integer")
                    )
                )
                typeConverter.jdkType2McpType(intMatrix2DType) shouldBe expectedIntMatrix2DType

                // Test 2D array of strings
                val stringMatrix2DType = Array<Array<String>>::class.java
                val expectedStringMatrix2DType = mapOf(
                    "type" to JsonPrimitive("array"),
                    "items" to mapOf(
                        "type" to JsonPrimitive("array"),
                        "items" to JsonPrimitive("String")
                    )
                )
                typeConverter.jdkType2McpType(stringMatrix2DType) shouldBe expectedStringMatrix2DType

                // Test 3D array of integers
                val intMatrix3DType = Array<Array<Array<Int>>>::class.java
                val expectedIntMatrix3DType = mapOf(
                    "type" to JsonPrimitive("array"),
                    "items" to mapOf(
                        "type" to JsonPrimitive("array"),
                        "items" to mapOf(
                            "type" to JsonPrimitive("array"),
                            "items" to JsonPrimitive("integer")
                        )
                    )
                )
                typeConverter.jdkType2McpType(intMatrix3DType) shouldBe expectedIntMatrix3DType
            }

            then("should throw IllegalArgumentException for unsupported types") {
                val unsupportedTypes = listOf(
                    Char::class.java,
                    Char::class.javaPrimitiveType,
                    Any::class.java,
                    List::class.java,
                    Map::class.java,
                )

                unsupportedTypes.forEach { type ->
                    shouldThrow<IllegalArgumentException> {
                        typeConverter.jdkType2McpType(type!!)
                    }
                }
            }
        }
    }
})
