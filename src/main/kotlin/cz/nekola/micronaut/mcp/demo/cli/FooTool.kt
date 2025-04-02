package cz.nekola.micronaut.mcp.demo.cli

import jakarta.inject.Singleton

@Singleton
class FooTool() {
    @Tool(
        name = "footoolik",
        description = "Toolik that Foos a lot"
    )
    fun toolik() = "toolik"

    @Tool(
        name = "footoolai2",
        description = "Toolik2 that Foos a lot"
    )
    fun toolika2(
        @ToolArg(name="arg1", description="Arg 1 desc") arg11: Int,
        @ToolArg("arg1", "Arg 2 desc") arg22: String
    ): String {
        return "toolik $arg11 $arg22"
    }


    @Tool(
        name = "footoolai3",
        description = "Toolik3 that Foos a lot"
    )
    fun toolika3(
        @ToolArg(name="arg1", description="Arg 1 desc") arg1: Int,
    ): String {
        return "toolik $arg1"
    }
}