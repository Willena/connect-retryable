package io.github.willena.connect;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeUtilTest {

    @Test
    void durationAsString() {
        assertEquals("00:00:01.025", TimeUtil.durationAsString(1025));
        assertEquals("00:00:10.025", TimeUtil.durationAsString(10025));
        assertEquals("00:01:40.025", TimeUtil.durationAsString(100025));
        assertEquals("00:16:40.025", TimeUtil.durationAsString(1000025));

    }

    @Test
    void testDurationAsString() {
        assertEquals("00:10:05.000", TimeUtil.durationAsString(Duration.of(10, ChronoUnit.MINUTES).plus(Duration.of(5, ChronoUnit.SECONDS))));
    }

    @Test
    void computeExponentialWaitTimeInMillis() {
        assertEquals(0, TimeUtil.computeExponentialWaitTimeInMillis(0, 0, 0));
        assertEquals(10, TimeUtil.computeExponentialWaitTimeInMillis(0, 10, 1000));
        assertEquals(20, TimeUtil.computeExponentialWaitTimeInMillis(1, 10, 1000));
        assertEquals(40, TimeUtil.computeExponentialWaitTimeInMillis(2, 10, 1000));
        assertEquals(1000, TimeUtil.computeExponentialWaitTimeInMillis(100, 10, 1000));

        assertThrows(IllegalArgumentException.class, () -> TimeUtil.computeExponentialWaitTimeInMillis(2, 1000, 10));
    }
}