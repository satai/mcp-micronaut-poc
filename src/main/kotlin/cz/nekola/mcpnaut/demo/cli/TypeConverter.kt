package cz.nekola.mcpnaut.demo.cli

import io.micronaut.core.annotation.NonNull
import jakarta.inject.Singleton
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Singleton
class TypeConverter {
    fun jdkType2McpType(type: @NonNull Class<out Any>): Map<String, JsonElement> =
        when (type) {
            String::class.java
                -> mapOf("type" to JsonPrimitive("String"))
            Int::class.javaPrimitiveType,
            Integer::class.java,
            Int::class.javaObjectType,
            Long::class.java,
            Long::class.javaPrimitiveType,
            Long::class.javaObjectType,
                -> mapOf("type" to JsonPrimitive("integer"))
            Double::class.javaPrimitiveType,
            Double::class.java,
            Double::class.javaObjectType,
            Float::class.javaPrimitiveType,
            Float::class.java,
            Float::class.javaObjectType,
                -> mapOf("type" to JsonPrimitive("number"))
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaObjectType,
            Boolean::class.java
                -> mapOf("type" to JsonPrimitive("boolean"))

            else -> when {
                type.isArray
                    -> mapOf(
                    "type" to JsonPrimitive("array"),
                    "items" to JsonObject(jdkType2McpType(type.componentType))
                )
                else
                    -> throw IllegalArgumentException("$type is not supported")
            }
        }
}
