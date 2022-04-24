package io.github.willena.connect.backoff;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExponentialBackoffTimerTest {

    @Test
    void computeBackoffMillis() {
        BackoffTimer timer = new ExponentialBackoffTimer(1000, TimeUnit.MILLISECONDS);
        assertEquals(1000, timer.computeBackoffMillis(0));
        assertEquals(2000, timer.computeBackoffMillis(1));
    }

    @Test
    void type() {
        BackoffTimer timer = new ExponentialBackoffTimer(1000, TimeUnit.MILLISECONDS);
        assertEquals("exponential (1000-86400000 ms)", timer.type());
    }
}