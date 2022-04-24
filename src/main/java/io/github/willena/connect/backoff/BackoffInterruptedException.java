package io.github.willena.connect.backoff;

import io.github.willena.connect.RetryException;

import java.time.Duration;
import java.util.Objects;


public class BackoffInterruptedException extends RetryException {
    private final Duration elapsedTime;

    public BackoffInterruptedException(String msg, Duration elapsedTime, Exception lastException) {
        super(msg, lastException);
        this.elapsedTime = Objects.requireNonNull(elapsedTime);
    }

    public Duration elapsedTime() {
        return this.elapsedTime;
    }
}

