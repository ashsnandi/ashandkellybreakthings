package uk.co.compendiumdev.performance.helpers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CsvResultWriter {

    private static final String OUTPUT_DIR = "performance_results";

    public static void write(String filename, List<PerformanceResult> results) {
        createOutputDirectory();
        String fullPath = OUTPUT_DIR + "/" + filename;
        try (PrintWriter writer = new PrintWriter(new FileWriter(fullPath))) {
            writer.println("objectCount,operationTimeMs,heapUsedMB,heapMaxMB");
            for (PerformanceResult result : results) {
                writer.printf("%d,%.3f,%d,%d%n",
                        result.objectCount,
                        result.operationTimeMs,
                        result.heapUsedMB,
                        result.heapMaxMB);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV file: " + fullPath, e);
        }
        System.out.println("[CsvResultWriter] Results written to: " + fullPath);
    }

    private static void createOutputDirectory() {
        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create output directory: " + OUTPUT_DIR, e);
        }
    }
}
