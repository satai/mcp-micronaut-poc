package cz.nekola.mcpnaut.demo.cli

import jakarta.inject.Singleton

@Singleton
class MultiTypeToolGroovy {
    @Tool(
        name = "singleParamTool_int_groovy",
        description = "Test tool with single int param (Groovy)"
    )
    String singleParamTool_int(
        @ToolArg(description = "int arg description") int param1
    ) {
        return "MultiTypeToolGroovy_int answer ${param1}"
    }

    @Tool(
        name = "singleParamTool_Integer_groovy",
        description = "Test tool with single Integer param (Groovy)"
    )
    String singleParamTool_Integer(
        @ToolArg(description = "Integer arg description") Integer param1
    ) {
        return "MultiTypeToolGroovy_Integer answer ${param1}"
    }

    @Tool(
        name = "singleParamTool_bool_groovy",
        description = "Test tool with single boolean param (Groovy)"
    )
    String singleParamTool_bool(
        @ToolArg(description = "Boolean arg description") boolean param1
    ) {
        return "MultiTypeToolGroovy_Bool answer ${param1}"
    }

    @Tool(
        name = "singleParamTool_long_groovy",
        description = "Test tool with single long param (Groovy)"
    )
    String singleParamTool_long(
        @ToolArg(description = "Long arg description") long param1
    ) {
        return "MultiTypeToolGroovy_Long answer ${param1}"
    }

    @Tool(
        name = "singleParamTool_float_groovy",
        description = "Test tool with single float param (Groovy)"
    )
    String singleParamTool_float(
        @ToolArg(description = "Float arg description") float param1
    ) {
        return "MultiTypeToolGroovy_Float answer ${param1}"
    }

    @Tool(
        name = "singleParamTool_double_groovy",
        description = "Test tool with single double param (Groovy)"
    )
    String singleParamTool_double(
        @ToolArg(description = "Double arg description") double param1
    ) {
        return "MultiTypeToolGroovy_Double answer ${param1}"
    }

    @Tool(
        name = "singleParamTool_string_groovy",
        description = "Test tool with single string param (Groovy)"
    )
    String singleParamTool_string(
        @ToolArg(description = "String arg description") String param1
    ) {
        return "MultiTypeToolGroovy_String answer ${param1}"
    }
}