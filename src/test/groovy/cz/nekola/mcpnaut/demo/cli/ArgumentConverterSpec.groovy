package cz.nekola.mcpnaut.demo.cli

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import io.micronaut.core.type.Argument
import io.modelcontextprotocol.sdk.model.CallToolRequest // Java SDK
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.util.Arrays

class ArgumentConverterSpec extends Specification {

    ArgumentConverter argumentConverter = new ArgumentConverter()
    @Shared JsonNodeFactory nodeFactory = JsonNodeFactory.instance

    private CallToolRequest createCallToolRequest(String paramName, JsonNode value) {
        ObjectNode args = nodeFactory.objectNode()
        args.set(paramName, value)
        // The Java SDK's CallToolRequest.Builder expects a Map<String, JsonNode>
        Map<String, JsonNode> argumentsMap = [:]
        argumentsMap.put(paramName, value)
        return CallToolRequest.newBuilder()
            .setName("test_tool_request")
            .setArguments(argumentsMap)
            .build()
    }

    private JsonNode toJsonNode(Object value) {
        if (value == null) return nodeFactory.nullNode()

        switch (value) {
            case Integer: return nodeFactory.numberNode((Integer) value)
            case Long: return nodeFactory.numberNode((Long) value)
            case Boolean: return nodeFactory.booleanNode((Boolean) value)
            case Float: return nodeFactory.numberNode((Float) value)
            case Double: return nodeFactory.numberNode((Double) value)
            case String: return nodeFactory.textNode((String) value)
        }

        if (value.getClass().isArray()) {
            ArrayNode arrayNode = nodeFactory.arrayNode()
            if (value instanceof int[]) {
                for (int i : (int[]) value) arrayNode.add(i)
            } else if (value instanceof long[]) {
                for (long l : (long[]) value) arrayNode.add(l)
            } else if (value instanceof boolean[]) {
                for (boolean b : (boolean[]) value) arrayNode.add(b)
            } else if (value instanceof float[]) {
                for (float f : (float[]) value) arrayNode.add(f)
            } else if (value instanceof double[]) {
                for (double d : (double[]) value) arrayNode.add(d)
            } else if (value instanceof String[]) {
                for (String s : (String[]) value) arrayNode.add(s)
            } else if (value instanceof Object[] && value.getClass().getComponentType().isArray()) { // 2D Object array
                 for (Object subArray : (Object[])value) {
                    arrayNode.add(toJsonNode(subArray)) // Recursive call
                 }
            } else if (value instanceof Object[]) { // 1D Object array (e.g. Integer[], Boolean[])
                 for (Object obj : (Object[])value) {
                    arrayNode.add(toJsonNode(obj))
                 }
            } else {
                throw new IllegalArgumentException("Unsupported array type for toJsonNode helper: " + value.getClass())
            }
            return arrayNode
        }
        throw new IllegalArgumentException("Unsupported type for toJsonNode helper: " + value.getClass())
    }

    @Unroll("should correctly convert #paramName (#type.simpleName) with value #inputValue to #expectedValue")
    def "correctly convert primitive, wrapper, and String types"() {
        given:
        CallToolRequest request = createCallToolRequest(paramName, toJsonNode(inputValue))
        Argument<?> arg = Argument.of(type, paramName)

        expect:
        argumentConverter.mcpValue2jvmValue(request, arg) == expectedValue

        where:
        paramName | type         | inputValue     | expectedValue
        "p_int"   | int          | 123            | 123
        "p_long"  | long         | 456L           | 456L
        "p_bool"  | boolean      | true           | true
        "p_float" | float        | 12.34f         | 12.34f
        "p_double"| double       | 56.78d         | 56.78d
        "p_string"| String       | "test"         | "test"
        "w_int"   | Integer      | 789            | 789
        "w_long"  | Long         | 101L           | 101L
        "w_bool"  | Boolean      | false          | false
        "w_float" | Float        | 43.21f         | 43.21f
        "w_double"| Double       | 87.65d         | 87.65d
        "null_str"| String       | null           | null // ArgumentConverter returns null if JsonNode is null
        "null_intW"| Integer    | null           | null
    }

    @Unroll("should correctly convert 1D array of #componentType.simpleName with name #paramName")
    def "correctly convert 1D array types"() {
        given:
        CallToolRequest request = createCallToolRequest(paramName, toJsonNode(valueArray))
        Argument<?> arg = Argument.of(arrayType, paramName)

        when:
        def result = argumentConverter.mcpValue2jvmValue(request, arg)

        then:
        result.getClass().isArray()
        // Groovy's == on arrays with same content but different object identity might be false for primitive arrays
        // Converting to list is a good way to check contents, or use Arrays.equals for primitives
        if (componentType.isPrimitive()) {
            // For primitive arrays, direct list conversion won't work well for comparison
            // Example: int[] vs List<Integer>. Need specific Arrays.equals
            if (result instanceof int[]) Arrays.equals(result as int[], valueArray as int[])
            else if (result instanceof long[]) Arrays.equals(result as long[], valueArray as long[])
            else if (result instanceof boolean[]) Arrays.equals(result as boolean[], valueArray as boolean[])
            else if (result instanceof float[]) Arrays.equals(result as float[], valueArray as float[])
            else if (result instanceof double[]) Arrays.equals(result as double[], valueArray as double[])
            else false // Should not happen based on where clause
        } else {
            (result as List) == (valueArray as List)
        }


        where:
        paramName        | arrayType      | componentType | valueArray
        "arr_int_p"      | int[].class     | int           | ([1, 2, 3] as int[])
        "arr_long_p"     | long[].class    | long          | ([10L, 20L] as long[])
        "arr_bool_p"     | boolean[].class | boolean       | ([true, false] as boolean[])
        "arr_float_p"    | float[].class   | float         | ([1.1f, 2.2f] as float[])
        "arr_double_p"   | double[].class  | double        | ([3.3d, 4.4d] as double[])
        "arr_string"     | String[].class  | String        | (["a", "b", "c"] as String[])
        "arr_int_w"      | Integer[].class | Integer       | ([Integer.valueOf(7), Integer.valueOf(8)] as Integer[])
        "arr_empty_int_p"| int[].class     | int           | ([] as int[])
        "arr_empty_str"  | String[].class  | String        | ([] as String[])
        "arr_null_val"   | String[].class  | String        | ([null, "test", null] as String[]) // Test with nulls in array
    }

    @Unroll("should correctly convert 2D array of #componentType.simpleName with name #paramName")
    def "correctly convert 2D array types"() {
        given:
        CallToolRequest request = createCallToolRequest(paramName, toJsonNode(valueMatrix))
        Argument<?> arg = Argument.of(matrixType, paramName)

        when:
        def result = argumentConverter.mcpValue2jvmValue(request, arg)

        then:
        result.getClass().isArray()
        result.getClass().getComponentType().isArray()
        Arrays.deepEquals(result as Object[], valueMatrix as Object[]) // Works for primitive and object 2D arrays

        where:
        paramName         | matrixType        | componentType | valueMatrix
        "matrix_int_p"    | int[][].class     | int           | ([[1,2] as int[], [3,4] as int[]] as int[][])
        "matrix_string"   | String[][].class  | String        | ([["a","b"] as String[], ["c","d"] as String[]] as String[][])
        "matrix_bool_p"   | boolean[][].class | boolean       | ([[true,false] as boolean[], [false,true] as boolean[]] as boolean[][])
        "matrix_empty_outer" | int[][].class  | int           | ([] as int[][]) // Empty outer array
        "matrix_empty_inner" | int[][].class  | int           | ([[] as int[], [] as int[]] as int[][]) // Empty inner arrays
        "matrix_null_inner" | String[][].class | String        | ([null, ["x","y"] as String[]] as String[][]) // Null inner array
    }


    @Unroll("should throw IllegalArgumentException for unsupported type #unsupportedType.simpleName")
    def "throw IllegalArgumentException for unsupported types"() {
        given:
        // The value for unsupported type doesn't matter much as type check comes first
        CallToolRequest request = createCallToolRequest("unsupportedParam", nodeFactory.textNode("irrelevant"))
        Argument<?> arg = Argument.of(unsupportedType, "unsupportedParam")

        when:
        argumentConverter.mcpValue2jvmValue(request, arg)

        then:
        thrown(IllegalArgumentException)

        where:
        unsupportedType << [Character.TYPE, Character, Object, List, Map, Date]
    }
}
