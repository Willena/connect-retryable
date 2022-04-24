package io.github.willena.connect.backoff;

import java.util.concurrent.TimeUnit;


public class ConstantBackoffTimer implements BackoffTimer {
    private final long backoffMillis;

    public ConstantBackoffTimer(long backoffTime, TimeUnit unit) {
        if (backoffTime <= 0L) {
            throw new IllegalArgumentException("The initial backoff time must be positive");
        }
        this.backoffMillis = unit.toMillis(backoffTime);
    }

    public long computeBackoffMillis(int attempts) {
        return this.backoffMillis;
    }


    public String type() {
        return "constant (" + this.backoffMillis + " ms)";
    }
}

