package io.github.willena.connect.counter;


import io.github.willena.connect.RetryException;

public class RetryCountExceeded extends RetryException {
    private final int maxAttempts;

    public RetryCountExceeded(String msg, int maxAttempts, Exception lastException) {
        super(msg, lastException);
        this.maxAttempts = maxAttempts;
    }

    public int attempts() {
        return this.maxAttempts;
    }
}
