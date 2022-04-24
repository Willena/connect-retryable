package io.github.willena.connect.backoff;

public interface BackoffTimer {
    long computeBackoffMillis(int attempts);

    String type();
}


