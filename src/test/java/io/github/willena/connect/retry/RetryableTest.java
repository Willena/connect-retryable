package io.github.willena.connect.retry;

import io.github.willena.connect.backoff.BackoffTimers;
import io.github.willena.connect.counter.RetryCountExceeded;
import io.github.willena.connect.counter.RetryTimeoutExceeded;
import org.apache.kafka.connect.errors.ConnectException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryableTest {

    @Test
    void builder() {
        Retryable.Builder builder = Retryable.builder();
        builder.withMaxRetries(1);
        builder.withMaxRetryTimeout(Duration.of(1, ChronoUnit.MINUTES));
        builder.withBackoffTimer(BackoffTimers.noBackoff());
        builder.when(Condition.never());
        Retryable r = builder.build();

        assertEquals(2, r.maxAttempts());
        assertEquals(1, r.maxRetries());
        assertEquals(Duration.ofMillis(60000), r.maxRetryTimeout().get());
        assertFalse(r.retryCondition().isTrue(new Exception("")));
    }

    @Test
    void maxAttempts() {
        Retryable.Builder builder = Retryable.builder();
        builder.withMaxRetries(2);
        builder.withMaxRetryTimeout(Duration.of(1, ChronoUnit.MINUTES));
        builder.withBackoffTimer(BackoffTimers.noBackoff());
        builder.when(Condition.always());
        Retryable r = builder.build();

        AtomicInteger counter = new AtomicInteger();

        assertThrows(RetryCountExceeded.class, () -> r.call("Doing things...", () -> {
            counter.getAndIncrement();
            throw new Exception("No ! ");
        }));
        assertEquals(3, counter.get());
    }


    @Test
    void maxRetryTimeout() {
        Retryable.Builder builder = Retryable.builder();
        builder.withMaxRetries(100);
        builder.withMaxRetryTimeout(Duration.of(1, ChronoUnit.MILLIS));
        builder.withBackoffTimer(BackoffTimers.noBackoff());
        builder.when(Condition.always());
        Retryable r = builder.build();

        AtomicInteger counter = new AtomicInteger();

        assertThrows(RetryTimeoutExceeded.class, () -> r.call("Doing things...", () -> {
            counter.getAndIncrement();
            throw new Exception("No ! ");
        }));
        assertTrue(counter.get() > 1);
        assertTrue(counter.get() < 100);
    }

    @Test
    void noRetry() {
        Retryable.Builder builder = Retryable.builder();
        builder.withMaxRetries(100);
        builder.withMaxRetryTimeout(Duration.of(1, ChronoUnit.MILLIS));
        builder.withBackoffTimer(BackoffTimers.noBackoff());
        builder.when(Condition.never());
        Retryable r = builder.build();

        AtomicInteger counter = new AtomicInteger();

        assertThrows(ConnectException.class, () -> r.call("Doing things...", () -> {
            counter.getAndIncrement();
            throw new Exception("No ! ");
        }));
        assertEquals(1, counter.get());
    }

    @Test
    void testToString() {
        Retryable.Builder builder = Retryable.builder();
        builder.withMaxRetries(100);
        builder.withMaxRetryTimeout(Duration.of(1, ChronoUnit.MILLIS));
        builder.withBackoffTimer(BackoffTimers.noBackoff());
        builder.when(Condition.always());
        Retryable r = builder.build();

        assertEquals("Call functions and retry up to 100 times or as many times up to 1 milliseconds using no backoff", r.toString());
    }
}