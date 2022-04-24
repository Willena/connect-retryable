package io.github.willena.connect.backoff;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BackoffTimersTest {

    @Test
    void noBackoff() {
        assertEquals("no", BackoffTimers.noBackoff().type());
        assertEquals(0, BackoffTimers.noBackoff().computeBackoffMillis(1));
    }

    @Test
    void constant() {
        BackoffTimer timer = BackoffTimers.constant(Duration.ofMillis(1000));
        assertEquals("constant (1000 ms)", timer.type());
        assertEquals(1000, timer.computeBackoffMillis(0));
    }

    @Test
    void exponential() {
        BackoffTimer timer = BackoffTimers.exponential(Duration.ofMillis(1000), Duration.ofMillis(86400000));
        assertEquals("exponential (1000-86400000 ms)", timer.type());
        assertEquals(1000, timer.computeBackoffMillis(0));


        BackoffTimer timer2 = BackoffTimers.exponential(Duration.ofMillis(1000));
        assertEquals("exponential (1000-86400000 ms)", timer2.type());
        assertEquals(1000, timer2.computeBackoffMillis(0));
    }

}