package cz.nekola.mcpnaut.demo.cli

import jakarta.inject.Singleton
import java.util.Arrays

@Singleton
class MultiTypeToolGroovy {
    // Primitives
    @Tool(name = "singleParamTool_int_groovy", description = "Test tool with single int param (Groovy)")
    String singleParamTool_int(@ToolArg(description = "int arg description") int param1) {
        return "MultiTypeToolGroovy_int answer ${param1}"
    }

    @Tool(name = "singleParamTool_boolean_groovy", description = "Test tool with single boolean param (Groovy)")
    String singleParamTool_boolean(@ToolArg(description = "boolean arg description") boolean param1) {
        return "MultiTypeToolGroovy_boolean answer ${param1}"
    }

    @Tool(name = "singleParamTool_long_groovy", description = "Test tool with single long param (Groovy)")
    String singleParamTool_long(@ToolArg(description = "long arg description") long param1) {
        return "MultiTypeToolGroovy_long answer ${param1}"
    }

    @Tool(name = "singleParamTool_float_groovy", description = "Test tool with single float param (Groovy)")
    String singleParamTool_float(@ToolArg(description = "float arg description") float param1) {
        return "MultiTypeToolGroovy_float answer ${param1}"
    }

    @Tool(name = "singleParamTool_double_groovy", description = "Test tool with single double param (Groovy)")
    String singleParamTool_double(@ToolArg(description = "double arg description") double param1) {
        return "MultiTypeToolGroovy_double answer ${param1}"
    }

    // Wrappers
    @Tool(name = "singleParamTool_Integer_groovy", description = "Test tool with single Integer param (Groovy)")
    String singleParamTool_Integer(@ToolArg(description = "Integer arg description") Integer param1) {
        return "MultiTypeToolGroovy_Integer answer ${param1}"
    }

    @Tool(name = "singleParamTool_Boolean_groovy", description = "Test tool with single Boolean param (Groovy)")
    String singleParamTool_Boolean(@ToolArg(description = "Boolean arg description") Boolean param1) {
        return "MultiTypeToolGroovy_Boolean answer ${param1}"
    }

    @Tool(name = "singleParamTool_Long_groovy", description = "Test tool with single Long param (Groovy)")
    String singleParamTool_Long(@ToolArg(description = "Long arg description") Long param1) {
        return "MultiTypeToolGroovy_Long answer ${param1}"
    }

    @Tool(name = "singleParamTool_Float_groovy", description = "Test tool with single Float param (Groovy)")
    String singleParamTool_Float(@ToolArg(description = "Float arg description") Float param1) {
        return "MultiTypeToolGroovy_Float answer ${param1}"
    }

    @Tool(name = "singleParamTool_Double_groovy", description = "Test tool with single Double param (Groovy)")
    String singleParamTool_Double(@ToolArg(description = "Double arg description") Double param1) {
        return "MultiTypeToolGroovy_Double answer ${param1}"
    }

    // String
    @Tool(name = "singleParamTool_string_groovy", description = "Test tool with single String param (Groovy)")
    String singleParamTool_string(@ToolArg(description = "String arg description") String param1) {
        return "MultiTypeToolGroovy_String answer ${param1}"
    }

    // 1D Arrays
    @Tool(name = "singleParamTool_string_array_groovy", description = "Test tool with single String array param (Groovy)")
    String singleParamTool_string_array(@ToolArg(description = "String array arg description") String[] param1) {
        return "MultiTypeToolGroovy_string_array answer ${Arrays.toString(param1)}"
    }

    @Tool(name = "singleParamTool_int_array_groovy", description = "Test tool with single int array param (Groovy)")
    String singleParamTool_int_array(@ToolArg(description = "int array arg description") int[] param1) {
        return "MultiTypeToolGroovy_int_array answer ${Arrays.toString(param1)}"
    }

    @Tool(name = "singleParamTool_long_array_groovy", description = "Test tool with single long array param (Groovy)")
    String singleParamTool_long_array(@ToolArg(description = "long array arg description") long[] param1) {
        return "MultiTypeToolGroovy_long_array answer ${Arrays.toString(param1)}"
    }

    @Tool(name = "singleParamTool_double_array_groovy", description = "Test tool with single double array param (Groovy)")
    String singleParamTool_double_array(@ToolArg(description = "double array arg description") double[] param1) {
        return "MultiTypeToolGroovy_double_array answer ${Arrays.toString(param1)}"
    }

    @Tool(name = "singleParamTool_float_array_groovy", description = "Test tool with single float array param (Groovy)")
    String singleParamTool_float_array(@ToolArg(description = "float array arg description") float[] param1) {
        return "MultiTypeToolGroovy_float_array answer ${Arrays.toString(param1)}"
    }

    @Tool(name = "singleParamTool_boolean_array_groovy", description = "Test tool with single boolean array param (Groovy)")
    String singleParamTool_boolean_array(@ToolArg(description = "boolean array arg description") boolean[] param1) {
        return "MultiTypeToolGroovy_boolean_array answer ${Arrays.toString(param1)}"
    }

    // 2D Arrays
    @Tool(name = "singleParamTool_string_array_array_groovy", description = "Test tool with single String[][] param (Groovy)")
    String singleParamTool_string_array_array(@ToolArg(description = "String[][] arg description") String[][] param1) {
        return "MultiTypeToolGroovy_string_array_array answer ${Arrays.deepToString(param1)}"
    }

    @Tool(name = "singleParamTool_int_array_array_groovy", description = "Test tool with single int[][] param (Groovy)")
    String singleParamTool_int_array_array(@ToolArg(description = "int[][] arg description") int[][] param1) {
        return "MultiTypeToolGroovy_int_array_array answer ${Arrays.deepToString(param1)}"
    }

    @Tool(name = "singleParamTool_boolean_array_array_groovy", description = "Test tool with single boolean[][] param (Groovy)")
    String singleParamTool_boolean_array_array(@ToolArg(description = "boolean[][] arg description") boolean[][] param1) {
        return "MultiTypeToolGroovy_boolean_array_array answer ${Arrays.deepToString(param1)}"
    }
}
