package cz.nekola.mcpnaut.demo.cli

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Unroll

@MicronautTest
class TypeConverterSpec extends Specification {

    @Inject
    TypeConverter typeConverter

    private final JsonNodeFactory nodeFactory = JsonNodeFactory.instance

    def "setup"() {
        // MicronautTest injects the bean, so no explicit setup needed for typeConverter
    }

    @Unroll("should correctly convert #jdkType.simpleName to MCP type #expectedMcpTypeMap")
    def "correctly convert basic JDK types to MCP types"() {
        expect:
        typeConverter.jdkType2McpType(jdkType) == expectedMcpTypeMap

        where:
        jdkType                   | expectedMcpTypeMap
        String                    | [type: nodeFactory.textNode("String")]
        Integer.TYPE              | [type: nodeFactory.textNode("integer")]
        Integer                   | [type: nodeFactory.textNode("integer")]
        Long.TYPE                 | [type: nodeFactory.textNode("integer")]
        Long                      | [type: nodeFactory.textNode("integer")]
        Double.TYPE               | [type: nodeFactory.textNode("number")]
        Double                    | [type: nodeFactory.textNode("number")]
        Float.TYPE                | [type: nodeFactory.textNode("number")]
        Float                     | [type: nodeFactory.textNode("number")]
        Boolean.TYPE              | [type: nodeFactory.textNode("boolean")]
        Boolean                   | [type: nodeFactory.textNode("boolean")]
    }

    @Unroll("should correctly convert 1D array type of #componentType.simpleName to MCP array type")
    def "correctly convert 1D array types"() {
        given:
        Class<?> arrayType = java.lang.reflect.Array.newInstance(componentType, 0).getClass()
        
        ObjectNode itemsMap = nodeFactory.objectNode()
        itemsMap.set("type", expectedComponentJsonPrimitive)

        Map<String, JsonNode> expectedMcpArrayType = [
            type: nodeFactory.textNode("array"),
            items: itemsMap
        ]

        expect:
        typeConverter.jdkType2McpType(arrayType) == expectedMcpArrayType

        where:
        componentType | expectedComponentJsonPrimitive
        Integer       | nodeFactory.textNode("integer")
        String        | nodeFactory.textNode("String")
        Boolean       | nodeFactory.textNode("boolean")
        Double        | nodeFactory.textNode("number")
    }

    def "correctly convert 2D array of Integers"() {
        given:
        Class<?> array2DType = int[][].class // Or Array.newInstance(int[].class, 0).getClass()

        ObjectNode finalItemsMap = nodeFactory.objectNode()
        finalItemsMap.set("type", nodeFactory.textNode("integer"))
        
        ObjectNode innerArrayMap = nodeFactory.objectNode()
        innerArrayMap.set("type", nodeFactory.textNode("array"))
        innerArrayMap.set("items", finalItemsMap)

        Map<String, JsonNode> expectedMcpArrayType = [
            type: nodeFactory.textNode("array"),
            items: innerArrayMap
        ]

        expect:
        typeConverter.jdkType2McpType(array2DType) == expectedMcpArrayType
    }
    
    def "correctly convert 2D array of Strings"() {
        given:
        Class<?> array2DType = String[][].class

        ObjectNode finalItemsMap = nodeFactory.objectNode()
        finalItemsMap.set("type", nodeFactory.textNode("String"))

        ObjectNode innerArrayMap = nodeFactory.objectNode()
        innerArrayMap.set("type", nodeFactory.textNode("array"))
        innerArrayMap.set("items", finalItemsMap)

        Map<String, JsonNode> expectedMcpArrayType = [
            type: nodeFactory.textNode("array"),
            items: innerArrayMap
        ]
        
        expect:
        typeConverter.jdkType2McpType(array2DType) == expectedMcpArrayType
    }

    // Example for 3D array was in Kotlin spec, can be added similarly if needed.

    @Unroll("should throw IllegalArgumentException for unsupported type #unsupportedType.simpleName")
    def "throw IllegalArgumentException for unsupported types"() {
        when:
        typeConverter.jdkType2McpType(unsupportedType)

        then:
        thrown(IllegalArgumentException)

        where:
        unsupportedType << [Character.TYPE, Character, Object, List, Map]
    }
}
