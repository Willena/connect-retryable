package io.github.willena.connect.counter;

import io.github.willena.connect.RetryException;

import java.time.Duration;
import java.util.Objects;


public class RetryTimeoutExceeded extends RetryException {
    private final Duration timeout;

    public RetryTimeoutExceeded(String msg, Duration timeout, Exception lastException) {
        super(msg, lastException);
        this.timeout = Objects.requireNonNull(timeout);
    }


    public Duration timeout() {
        return this.timeout;
    }
}

