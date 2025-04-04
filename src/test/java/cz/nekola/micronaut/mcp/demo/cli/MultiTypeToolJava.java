package cz.nekola.micronaut.mcp.demo.cli;

import jakarta.inject.Singleton;

import java.util.Arrays;

@Singleton
public class MultiTypeToolJava {
    @Tool(
        name = "singleParamTool_int_java",
        description = "Test tool with single int param (Java)"
    )
    public String singleParamTool_int(
        @ToolArg(description = "int arg description") int param1
    ) {
        return "MultiTypeToolJava_int answer " + param1;
    }

    @Tool(
        name = "singleParamTool_Integer_java",
        description = "Test tool with single Integer param (Java)"
    )
    public String singleParamTool_Integer(
        @ToolArg(description = "Integer arg description") Integer param1
    ) {
        return "MultiTypeToolJava_Integer answer " + param1;
    }

    @Tool(
        name = "singleParamTool_bool_java",
        description = "Test tool with single boolean param (Java)"
    )
    public String singleParamTool_bool(
        @ToolArg(description = "Boolean arg description") boolean param1
    ) {
        return "MultiTypeToolJava_Bool answer " + param1;
    }

    @Tool(
        name = "singleParamTool_long_java",
        description = "Test tool with single long param (Java)"
    )
    public String singleParamTool_long(
        @ToolArg(description = "Long arg description") long param1
    ) {
        return "MultiTypeToolJava_Long answer " + param1;
    }

    @Tool(
        name = "singleParamTool_float_java",
        description = "Test tool with single float param (Java)"
    )
    public String singleParamTool_float(
        @ToolArg(description = "Float arg description") float param1
    ) {
        return "MultiTypeToolJava_Float answer " + param1;
    }

    @Tool(
        name = "singleParamTool_double_java",
        description = "Test tool with single double param (Java)"
    )
    public String singleParamTool_double(
        @ToolArg(description = "Double arg description") double param1
    ) {
        return "MultiTypeToolJava_Double answer " + param1;
    }

    @Tool(
        name = "singleParamTool_string_java",
        description = "Test tool with single string param (Java)"
    )
    public String singleParamTool_string(
        @ToolArg(description = "String arg description") String param1
    ) {
        return "MultiTypeToolJava_String answer " + param1;
    }

    @Tool(
            name = "singleParamTool_array_of_arrays_of_strings_java",
            description = "Test tool with single array of array of strings param (Java)"
    )
    public String singleParamTool_array_of_arrays_of_strings(
            @ToolArg(description = "Array of array of Strings arg description") String[][] param1
    ) {
        return "MultiTypeToolJava_ArrayOfArrayyOfStrings answer " + Arrays.deepToString(param1);
    }
}
