package io.github.willena.connect.backoff;

import io.github.willena.connect.TimeUtil;

import java.util.concurrent.TimeUnit;

public class ExponentialBackoffTimer implements BackoffTimer {
    protected static final long MAX_WAIT_TIME = TimeUnit.HOURS.toMillis(24L);

    private final long initialBackoffMillis;

    private final long maxWaitTimeMillis;

    public ExponentialBackoffTimer(long initialBackoffDuration, TimeUnit unit) {
        this(initialBackoffDuration, unit.convert(MAX_WAIT_TIME, TimeUnit.MILLISECONDS), unit);
    }

    public ExponentialBackoffTimer(long initialBackoffDuration, long maxWaitTime, TimeUnit unit) {
        if (initialBackoffDuration < 0L) {
            throw new IllegalArgumentException("The initial backoff time may not be negative");
        }
        if (maxWaitTime < initialBackoffDuration) {
            throw new IllegalArgumentException("The max timer must be larger than initial backoff");
        }
        this.initialBackoffMillis = unit.toMillis(initialBackoffDuration);
        this.maxWaitTimeMillis = unit.toMillis(maxWaitTime);
    }

    public long computeBackoffMillis(int attempts) {
        if (attempts < 0) {
            return this.initialBackoffMillis;
        }

        return TimeUtil.computeExponentialWaitTimeInMillis(attempts, this.initialBackoffMillis, this.maxWaitTimeMillis);
    }


    public String type() {
        return String.format("exponential (%s-%s ms)", this.initialBackoffMillis, this.maxWaitTimeMillis);
    }


}


