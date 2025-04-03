package cz.nekola.micronaut.mcp.demo.cli

import io.micronaut.core.annotation.NonNull
import jakarta.inject.Singleton
import kotlinx.serialization.json.JsonPrimitive

@Singleton
class TypeConverter {
    fun jdkType2McpType(type: @NonNull Class<out Any>) =
        when (type) {
            String::class.java
                -> JsonPrimitive("String")
            Int::class.javaPrimitiveType,
            Integer::class.java,
            Long::class.java,
            Long::class.javaPrimitiveType
                -> JsonPrimitive("integer")
            Double::class.javaPrimitiveType,
            Double::class.java,
            Float::class.javaPrimitiveType,
            Float::class.javaPrimitiveType
                -> JsonPrimitive("number")
            Boolean::class.javaPrimitiveType,
            Boolean::class.java
                -> JsonPrimitive("boolean")
            else -> throw IllegalArgumentException("$type is not supported")
        }
}
