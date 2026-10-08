# Java Order Matching Engine

A Java 21 portfolio project implementing a single-instrument limit
order matching engine with deterministic command replay, immutable
order-book snapshots, and local performance benchmarks.

## Features

- Price-time priority: best price first, then FIFO at the same price.
- Full and partial order fills.
- Trades executed at the resting order's price.
- Cancellation of active orders.
- Duplicate order ID rejection, including previously filled or cancelled IDs.
- Integer price ticks and quantities.
- Replay of SUBMIT and CANCEL commands from a text file.
- Replay comparison of trades, cancellation outcomes, active order count,
  and the final order-book snapshot.
- 17 JUnit tests covering matching, replay, file validation, and snapshots.

## Requirements

- JDK 21
- Maven

## Build and test

```powershell
mvn test
```

## Run the matching demo

```powershell
mvn compile
java -cp target/classes com.revathi.matching.Demo
```

## Run file-based replay

```powershell
java -cp target/classes com.revathi.matching.ReplayDemo data/sample-commands.csv
```

Command format, without a header:

```text
SUBMIT,1,SELL,10000,5
SUBMIT,2,BUY,10000,3
CANCEL,1
```

Blank lines and lines beginning with # are ignored.
Malformed commands report their physical line number.
Invalid duplicate submissions stop replay.

## Design

The engine uses TreeMap price levels and ArrayDeque FIFO queues.
A HashMap indexes active orders, while a HashSet tracks used order IDs.

Cancellation scans the queue at the order's price level, so it is
linear in the number of orders at that level.

Snapshots preserve price priority and FIFO ordering and contain
immutable copies of remaining order data.

## Benchmarks

```powershell
java -cp target/classes com.revathi.matching.PerformanceBenchmark
java -cp target/classes com.revathi.matching.MixedWorkloadBenchmark
```

Both benchmarks use 3 warmup batches and 5 measured batches.
Each batch starts with a fresh engine. Timing includes order creation
and engine calls; engine construction, result validation, and console
output are outside the timed section.

Observed locally on Windows 11 with Java 21.0.12.1:

| Workload | Operations per batch | Aggregate throughput |
|---|---:|---:|
| Alternating SELL/BUY pairs | 100,000 submissions | 3,367,774 orders/sec |
| Book build, partial fills, reverse cancellations | 20,000 submissions + 10,000 cancellations | 4,877,795 operations/sec |

Raw output is stored in the results directory.

These are preliminary batch measurements using System.nanoTime,
not JMH benchmarks. Results depend on hardware, JVM behaviour,
and system load. The two workloads measure different operation mixes.
Average batch time per order is not individual order latency.

## Scope and limitations

- Single instrument and single-threaded execution.
- Limit orders only.
- No network API, live market integration, or persistent live command journal.
- File replay reconstructs results from a supplied command sequence.
- Used order IDs remain in memory for the engine's lifetime.
- Book snapshots omit used-ID history and trade sequence state;
  they are comparison views, not recovery checkpoints.
- No individual latency percentiles or production performance guarantees.