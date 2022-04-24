package io.github.willena.connect.counter;

import io.github.willena.connect.backoff.BackoffTimers;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class RetryCounterTest {

    @Test
    void getters() {
        try (RetryCounter counter = RetryCounter.using(0, 2, 10000, TimeUnit.MILLISECONDS, BackoffTimers.noBackoff())) {
            assertEquals(2, counter.maxRetries());
            assertEquals(0, counter.retries());
            assertEquals(10000, counter.timeRemaining());
            assertEquals(BackoffTimers.noBackoff().type(), counter.type());
        }
    }

    @Test
    void counterWait() {
        try (RetryCounter counter = RetryCounter.using(0, 2, 10000, TimeUnit.MILLISECONDS, BackoffTimers.noBackoff())) {
            assertTimeout(Duration.ofMillis(1200), () -> counter.sleep(1000));
        }
    }

    @Test
    void timeoutTest() {
        try (RetryCounter counter = RetryCounter.using(0, 2, 10000, TimeUnit.MILLISECONDS, BackoffTimers.noBackoff())) {
            counter.backoffAfterFailedAttempt("Fail ! ");
            assertTimeout(Duration.ofMillis(1200), () -> counter.sleep(1000));
            assertTrue(counter.timeRemaining() < 9000, String.format("%s < %s", counter.timeRemaining(), 9000));
        }
    }
}