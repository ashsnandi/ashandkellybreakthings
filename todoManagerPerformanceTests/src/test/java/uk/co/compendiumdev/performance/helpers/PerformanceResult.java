package uk.co.compendiumdev.performance.helpers;

public class PerformanceResult {

    public final int objectCount;
    public final double operationTimeMs;
    public final long heapUsedMB;
    public final long heapMaxMB;

    public PerformanceResult(int objectCount, double operationTimeMs, long heapUsedMB, long heapMaxMB) {
        this.objectCount = objectCount;
        this.operationTimeMs = operationTimeMs;
        this.heapUsedMB = heapUsedMB;
        this.heapMaxMB = heapMaxMB;
    }
}
