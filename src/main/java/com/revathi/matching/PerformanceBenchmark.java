package com.revathi.matching;

import java.util.Locale;

public final class PerformanceBenchmark {

    private static final int PAIRS = 50_000;
    private static final int ORDERS = PAIRS * 2;
    private static final int WARMUP_RUNS = 3;
    private static final int MEASURED_RUNS = 5;

    private record RunResult(long elapsedNanos, long trades) {
    }

    private static RunResult runBatch() {
        MatchingEngine engine = new MatchingEngine();
        long tradeCount = 0;

        long start = System.nanoTime();

        for (int i = 0; i < PAIRS; i++) {
            long sellId = 2L * i + 1;
            long buyId = sellId + 1;
            long price = 10000 + (i % 100);

            tradeCount += engine.submit(
                    new Order(sellId, Side.SELL, price, 10)
            ).size();

            tradeCount += engine.submit(
                    new Order(buyId, Side.BUY, price, 10)
            ).size();
        }

        long elapsed = System.nanoTime() - start;

        // Validate outside the timed section.
        if (tradeCount != PAIRS || engine.activeOrderCount() != 0) {
            throw new IllegalStateException(
                    "Unexpected benchmark results"
            );
        }

        return new RunResult(elapsed, tradeCount);
    }

    public static void main(String[] args) {
        System.out.println("Workload: alternating SELL/BUY pairs");
        System.out.println("Orders per run: " + ORDERS);
        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name"));

        for (int i = 0; i < WARMUP_RUNS; i++) {
            runBatch();
        }

        System.out.println("Warmup complete.");

        long totalNanos = 0;

        for (int i = 1; i <= MEASURED_RUNS; i++) {
            RunResult result = runBatch();
            totalNanos += result.elapsedNanos();

            double seconds = result.elapsedNanos() / 1_000_000_000.0;

            System.out.printf(
                    Locale.ROOT,
                    "Run %d: %.2f ms | %.0f orders/sec | trades=%d%n",
                    i,
                    seconds * 1000,
                    ORDERS / seconds,
                    result.trades()
            );
        }

        double totalSeconds = totalNanos / 1_000_000_000.0;
        long totalOrders = (long) ORDERS * MEASURED_RUNS;

        System.out.printf(
                Locale.ROOT,
                "Aggregate throughput: %.0f orders/sec%n",
                totalOrders / totalSeconds
        );

        System.out.printf(
                Locale.ROOT,
                "Average batch time per order: %.2f ns%n",
                (double) totalNanos / totalOrders
        );
    }
}