package io.github.willena.connect.backoff;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public final class BackoffTimers {

    public static BackoffTimer noBackoff() {
        return new NoBackoffTimer();
    }

    public static BackoffTimer constant(Duration backoffDuration) {
        Objects.requireNonNull(backoffDuration);
        return new ConstantBackoffTimer(backoffDuration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public static BackoffTimer exponential(Duration initialBackoff) {
        Objects.requireNonNull(initialBackoff);
        return new ExponentialBackoffTimer(initialBackoff.toMillis(), TimeUnit.MILLISECONDS);
    }

    public static BackoffTimer exponential(Duration initialBackoff, Duration maxRetryTime) {
        Objects.requireNonNull(initialBackoff);
        Objects.requireNonNull(maxRetryTime);
        return new ExponentialBackoffTimer(initialBackoff.toMillis(), maxRetryTime.toMillis(), TimeUnit.MILLISECONDS);
    }
}

