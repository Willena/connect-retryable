package io.github.willena.connect.backoff;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConstantBackoffTimerTest {

    @Test
    void computeBackoffMillis() {
        BackoffTimer timer = new ConstantBackoffTimer(1000, TimeUnit.MILLISECONDS);
        assertEquals(1000, timer.computeBackoffMillis(0));
        assertEquals(1000, timer.computeBackoffMillis(1));
        assertEquals(1000, timer.computeBackoffMillis(10000));
    }

    @Test
    void type() {
        BackoffTimer timer = new ConstantBackoffTimer(1000, TimeUnit.MILLISECONDS);
        assertEquals("constant (1000 ms)", timer.type());
    }
}