package io.github.willena.connect.backoff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoBackoffTimerTest {

    @Test
    void computeBackoffMillis() {

        NoBackoffTimer bt = new NoBackoffTimer();
        assertEquals(0, bt.computeBackoffMillis(1));
        assertEquals(0, bt.computeBackoffMillis(0));
    }

    @Test
    void type() {
        NoBackoffTimer bt = new NoBackoffTimer();
        assertEquals("no", bt.type());
    }
}