package uk.co.compendiumdev.sparkstart;

import java.io.File;
import java.io.IOException;

public class Environment {

    private static final String BASE_URL = "http://localhost:4567";
    private static final int SERVER_PORT = 4567;
    private static Process serverProcess;

    public static String getEnv(String path) {
        return getBaseUri() + path;
    }

    public static String getBaseUri() {
        if (Port.inUse("localhost", SERVER_PORT)) {
            return BASE_URL;
        }

        startServer();
        return BASE_URL;
    }

    private static void startServer() {
        String jarPath = findJarPath();

        try {
            serverProcess = new ProcessBuilder("java", "-jar", jarPath)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start server JAR: " + jarPath, e);
        }

        waitForServerReady();
        Runtime.getRuntime().addShutdownHook(new Thread(Environment::stopServer));
    }

    private static void waitForServerReady() {
        int maxAttempts = 20;
        for (int i = 0; i < maxAttempts; i++) {
            if (Port.inUse("localhost", SERVER_PORT)) {
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for server", e);
            }
        }
        throw new RuntimeException("Server did not start within 10 seconds");
    }

    private static String findJarPath() {
        String[] searchPaths = {
                "../runTodoManagerRestAPI-1.5.5.jar",
                "runTodoManagerRestAPI-1.5.5.jar",
                "../../runTodoManagerRestAPI-1.5.5.jar"
        };

        for (String path : searchPaths) {
            File file = new File(path);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }

        throw new RuntimeException(
                "Cannot find runTodoManagerRestAPI-1.5.5.jar. "
                + "Please start the server manually: java -jar runTodoManagerRestAPI-1.5.5.jar");
    }

    public static void stopServer() {
        if (serverProcess != null && serverProcess.isAlive()) {
            serverProcess.destroy();
        }
    }
}
