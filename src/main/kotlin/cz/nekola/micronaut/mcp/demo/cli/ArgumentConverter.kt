package cz.nekola.micronaut.mcp.demo.cli

import io.micronaut.core.annotation.NonNull
import io.micronaut.core.type.Argument
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import jakarta.inject.Singleton
import kotlinx.serialization.json.*

/**
 * Converts JSON arguments from a CallToolRequest to their appropriate Java types.
 */
@Singleton
class ArgumentConverter {
    /**
     * Converts a JSON element from a CallToolRequest to the appropriate Java type based on the parameter type.
     *
     * @param call The CallToolRequest containing the arguments as JSON elements
     * @param parameter The parameter information including its type
     * @return The converted value, or null if the conversion is not possible
     * @throws IllegalArgumentException if the parameter type is not supported
     */
    fun convert(
        call: CallToolRequest,
        parameter: @NonNull Argument<*>,
    ): Any? {
        val element: JsonElement = call.arguments[parameter.name]!!
        return when (parameter.type) {
            Int::class.javaPrimitiveType,
            Int::class.java,
                -> element.jsonPrimitive.intOrNull
            Long::class.javaPrimitiveType,
            Long::class.java,
                -> element.jsonPrimitive.longOrNull
            Boolean::class.javaPrimitiveType,
            Boolean::class.java
                -> element.jsonPrimitive.booleanOrNull
            Float::class.javaPrimitiveType,
            Float::class.java
                    -> element.jsonPrimitive.floatOrNull
            Double::class.javaPrimitiveType,
            Double::class.java
                    -> element.jsonPrimitive.doubleOrNull
            String::class.java
                -> element.jsonPrimitive.contentOrNull
            else
                -> throw IllegalArgumentException("Unsupported type ${parameter.type}")
        }
    }
}
