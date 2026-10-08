package com.revathi.matching;

import java.util.Objects;

public sealed interface EngineCommand
        permits EngineCommand.Submit, EngineCommand.Cancel {

    record Submit(Order order) implements EngineCommand {
        public Submit {
            Objects.requireNonNull(order, "Order is required");
        }
    }

    record Cancel(long orderId) implements EngineCommand {
        public Cancel {
            if (orderId <= 0) {
                throw new IllegalArgumentException(
                        "Order ID must be positive"
                );
            }
        }
    }
}