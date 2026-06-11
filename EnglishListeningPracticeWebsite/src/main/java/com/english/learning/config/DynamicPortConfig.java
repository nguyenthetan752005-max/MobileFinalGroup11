package com.english.learning.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Dynamic Port Configuration.
 *
 * Checks if the configured port (default 8080) is available.
 * If occupied (e.g. by System on Windows), automatically finds
 * the next available port and uses that instead.
 *
 * This avoids "Address already in use" errors on startup.
 */
@Component
@Slf4j
public class DynamicPortConfig implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    private static final int DEFAULT_PORT = 8080;
    private static final int MAX_PORT = 9000;

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        int port = DEFAULT_PORT;

        if (!isPortAvailable(port)) {
            log.warn("Port {} is already in use. Searching for an available port...", port);
            port = findAvailablePort(port + 1);
            log.info("Using alternative port: {}", port);
        } else {
            log.info("Port {} is available. Starting on default port.", port);
        }

        factory.setPort(port);
    }

    /**
     * Check if a specific port is available for binding.
     */
    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Find the next available port starting from the given port.
     */
    private int findAvailablePort(int startPort) {
        for (int port = startPort; port <= MAX_PORT; port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        // Fallback: let the OS assign a random port
        log.warn("No available port found between {} and {}. Using random port (0).", startPort, MAX_PORT);
        return 0;
    }
}
