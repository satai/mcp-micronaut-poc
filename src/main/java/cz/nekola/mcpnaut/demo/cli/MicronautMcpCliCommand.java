package cz.nekola.mcpnaut.demo.cli;

import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;
import io.modelcontextprotocol.sdk.server.McpServer; // Java SDK
import io.modelcontextprotocol.sdk.transport.StdioTransport; // Java SDK

import jakarta.inject.Inject;
import picocli.CommandLine.Command;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.concurrent.CountDownLatch;

@Command(
        name = "micronaut-mcp-cli", description = "...",
        mixinStandardHelpOptions = true
)
public class MicronautMcpCliCommand implements Runnable {

    @Inject
    ApplicationContext applicationContext; // Field injection

    @Override
    public void run() {
        // Java SDK StdioTransport with standard Java IO
        StdioTransport transport = new StdioTransport(
                new BufferedInputStream(System.in),
                new BufferedOutputStream(System.out)
        );

        ServerWrapper serverWrapper = applicationContext.getBean(ServerWrapper.class); // Java ServerWrapper
        McpServer mcpServer = serverWrapper.getServer(); // Use getter for McpServer

        CountDownLatch doneSignal = new CountDownLatch(1);

        // Assuming the Java SDK's McpServer has a way to register a close handler.
        // This might be named differently, e.g., addDisconnectionListener, setCloseCallback, etc.
        // For this example, let's assume a method like `addCloseHandler`.
        // If no such handler exists, the connect() method might be blocking and
        // this latch logic might need to be re-evaluated or handled with try-with-resources
        // if McpServer or StdioTransport is AutoCloseable.
        mcpServer.addCloseHandler(() -> {
            System.out.println("Server closing down...");
            doneSignal.countDown();
        });
        
        try {
            System.out.println("MCP Server connecting via STDIO...");
            mcpServer.connect(transport); // This call might be blocking or start a new thread
            
            // If connect() is non-blocking and starts the server in background threads,
            // this await() will keep the main thread alive until the server closes.
            // If connect() is blocking, this await might only be reached after it returns (i.e., after server disconnects).
            System.out.println("MCP Server connected. Waiting for close signal...");
            doneSignal.await(); // Wait until onClose callback is triggered
            System.out.println("Server closed.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Server run interrupted: " + e.getMessage());
        } catch (Exception e) {
            // Catch other potential exceptions from connect() or during server operation
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PicocliRunner.run(MicronautMcpCliCommand.class, args);
    }
}
