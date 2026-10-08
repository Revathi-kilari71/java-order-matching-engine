package com.revathi.matching;

import java.util.Locale;

public final class MixedWorkloadBenchmark {

    private static final int RESTING_ORDERS = 10_000;
    private static final int OPERATIONS = RESTING_ORDERS * 3;
    private static final int WARMUP_RUNS = 3;
    private static final int MEASURED_RUNS = 5;

    private record RunResult(
            long elapsedNanos,
            long trades,
            int cancellations
    ) {
    }

    private static RunResult runBatch() {
        MatchingEngine engine = new MatchingEngine();
        long trades = 0;
        int cancellations = 0;

        long start = System.nanoTime();

        // Build 100 price levels, each with 100 sell orders.
        for (int i = 0; i < RESTING_ORDERS; i++) {
            trades += engine.submit(new Order(
                    i + 1L,
                    Side.SELL,
                    10000 + (i % 100),
                    10
            )).size();
        }

        // Each buy fills half of the current best sell order.
        // Two buys therefore fully fill one resting sell order.
        for (int i = 0; i < RESTING_ORDERS; i++) {
            trades += engine.submit(new Order(
                    RESTING_ORDERS + i + 1L,
                    Side.BUY,
                    10099,
                    5
            )).size();
        }

        // Cancel in reverse ID order to exercise queue removal.
        for (long id = RESTING_ORDERS; id >= 1; id--) {
            if (engine.cancel(id)) {
                cancellations++;
            }
        }

        long elapsed = System.nanoTime() - start;

        if (trades != RESTING_ORDERS
                || cancellations != RESTING_ORDERS / 2
                || engine.activeOrderCount() != 0) {
            throw new IllegalStateException(
                    "Unexpected mixed workload results"
            );
        }

        return new RunResult(elapsed, trades, cancellations);
    }

    public static void main(String[] args) {
        System.out.println(
                "Workload: build book, partial fills, reverse cancellations"
        );
        System.out.println("Submit calls per run: 20000");
        System.out.println("Cancel calls per run: 10000");
        System.out.println("Operations per run: " + OPERATIONS);
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
                    "Run %d: %.2f ms | %.0f operations/sec"
                            + " | trades=%d | cancellations=%d%n",
                    i,
                    seconds * 1000,
                    OPERATIONS / seconds,
                    result.trades(),
                    result.cancellations()
            );
        }

        double totalSeconds = totalNanos / 1_000_000_000.0;

        System.out.printf(
                Locale.ROOT,
                "Aggregate throughput: %.0f operations/sec%n",
                (long) OPERATIONS * MEASURED_RUNS / totalSeconds
        );
    }
}