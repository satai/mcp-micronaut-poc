package cz.nekola.micronaut.mcp.demo.cli

import io.micronaut.context.annotation.DefaultScope
import io.micronaut.context.annotation.Executable
import io.micronaut.context.processor.ExecutableMethodProcessor
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import jakarta.inject.Singleton

@MustBeDocumented // FIXME
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Executable(processOnStartup = true)
annotation class Tool()

@MustBeDocumented // FIXME
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
@Executable(processOnStartup = true)
annotation class ToolArg()

@Singleton
class FooTool() {
    @Tool
    fun toolik() {
        println("toolik")
    }

    @Tool
    fun toolik2(@ToolArg arg1: Int, @ToolArg arg2: String) {
        println("toolik $arg1 $arg2")
    }
}

@Singleton
class ToolBuilder: ExecutableMethodProcessor<Tool> {
    override fun process(beanDefinition: BeanDefinition<*>?, method: ExecutableMethod<*, *>?) {
        println("$beanDefinition, $method")
        method!!.arguments.forEach {
            println("Arg: $it : ${it.type} ${it.name} ${it.annotationMetadata.annotationNames}")
        }
    }

}

