package cz.nekola.mcpnaut.demo.cli

import jakarta.inject.Singleton

@Singleton
class MultiTypeToolKotlin {
    @Tool(
        name = "singleParamTool_int",
        description = "Test tool with single int param"
    )
    fun singleParamTool_int(
        @ToolArg("int arg description") param1: Int
    ) = "MultiTypeToolKotlin_int answer $param1"

    @Tool(
        name = "singleParamTool_Integer",
        description = "Test tool with single Integer param"
    )
    fun singleParamTool_Integer(
        @ToolArg("Integer arg description") param1: Integer
    ) = "MultiTypeToolKotlin_Integer answer $param1"

    @Tool(
        name = "singleParamTool_bool",
        description = "Test tool with single bool param"
    )
    fun singleParamTool_bool(
        @ToolArg("Boolean arg description") param1: Boolean
    ) = "MultiTypeToolKotlin_Bool answer $param1"

    @Tool(
        name = "singleParamTool_long",
        description = "Test tool with single long param"
    )
    fun singleParamTool_long(
        @ToolArg("Long arg description") param1: Long
    ) = "MultiTypeToolKotlin_Long answer $param1"

    @Tool(
        name = "singleParamTool_float",
        description = "Test tool with single float param"
    )
    fun singleParamTool_float(
        @ToolArg("Float arg description") param1: Float
    ) = "MultiTypeToolKotlin_Float answer $param1"

    @Tool(
        name = "singleParamTool_double",
        description = "Test tool with single double param"
    )
    fun singleParamTool_double(
        @ToolArg("Double arg description") param1: Double
    ) = "MultiTypeToolKotlin_Double answer $param1"

    @Tool(
        name = "singleParamTool_string",
        description = "Test tool with single string param"
    )
    fun singleParamTool_string(
        @ToolArg("String arg description") param1: String
    ) = "MultiTypeToolKotlin_String answer $param1"

    @Tool(
        name = "singleParamTool_array_of_strings",
        description = "Test tool with single array of strings param"
    )
    fun singleParamTool_array_of_strings(
        @ToolArg("Array of Strings arg description") param1: Array<String>
    ) = "MultiTypeToolKotlin_Array_of_Strings answer ${param1.joinToString()}"

    @Tool(
        name = "singleParamTool_array_of_ints",
        description = "Test tool with single array of ints param"
    )
    fun singleParamTool_array_of_ints(
        @ToolArg("Array of Ints arg description") param1: Array<Int>
    ) = "MultiTypeToolKotlin_Array_of_Ints answer ${param1.joinToString()}"

    @Tool(
        name = "singleParamTool_array_of_array_of_ints",
        description = "Test tool with single array of array of ints param"
    )
    fun singleParamTool_array_of_arrays_of_ints(
        @ToolArg("Array of Ints arg description") param1: Array<Array<Int>>
    ) = """MultiTypeToolKotlin_Array_of_Array_of_Ints answer ${param1.map {  "[${it.joinToString()}]" }}"""

}
