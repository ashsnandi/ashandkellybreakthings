package uk.co.compendiumdev.performance.helpers;

public class SystemMonitor {

    private static final long BYTES_PER_MB = 1024L * 1024L;

    public static long getHeapUsedMB() {
        Runtime rt = Runtime.getRuntime();
        long usedBytes = rt.totalMemory() - rt.freeMemory();
        return usedBytes / BYTES_PER_MB;
    }

    public static long getHeapMaxMB() {
        return Runtime.getRuntime().maxMemory() / BYTES_PER_MB;
    }
}
