package cz.nekola.mcpnaut.demo.cli

import jakarta.inject.Singleton
// Tool and ToolArg are in the same package, so explicit imports might not be needed.
// If they were, they would be:
// import cz.nekola.mcpnaut.demo.cli.Tool 
// import cz.nekola.mcpnaut.demo.cli.ToolArg

@Singleton
class FooTool {
    @Tool(
        name = "footoolik",
        description = "Toolik that Foos a lot"
    )
    String toolik() {
        return "toolik"
    }

    @Tool(
        name = "footoolai2",
        description = "Toolik2 that Foos a lot"
    )
    String toolika2(
        @ToolArg(description="Arg 1 desc") int arg11,
        @ToolArg(description="Arg 2 desc") String arg22
    ) {
        return "toolik ${arg11} ${arg22}"
    }

    @Tool(
        name = "footoolai3",
        description = "Toolik3 that Foos a lot"
    )
    String toolika3(
        @ToolArg(description="Arg 1 desc") int arg1
    ) {
        return "toolik ${arg1}"
    }
}
