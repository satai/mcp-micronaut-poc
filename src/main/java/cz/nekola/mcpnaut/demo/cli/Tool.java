package cz.nekola.mcpnaut.demo.cli;

import io.micronaut.context.annotation.Executable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Executable(processOnStartup = true)
public @interface Tool {
    String name();
    String description();
}
