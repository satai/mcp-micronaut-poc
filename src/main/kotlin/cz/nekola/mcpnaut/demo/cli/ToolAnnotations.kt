package cz.nekola.mcpnaut.demo.cli

import io.micronaut.context.annotation.Executable

@MustBeDocumented // FIXME
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Executable(processOnStartup = true)
annotation class Tool(
    val name: String,
    val description: String,
)

@MustBeDocumented // FIXME
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
@Executable(processOnStartup = true)
annotation class ToolArg(
    // val name: String, TODO don't require, default to arg name
    val description: String
)