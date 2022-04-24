package io.github.willena.connect;

import java.time.Duration;

public class TimeUtil {
    public static String durationAsString(long millis) {
        return durationAsString(Duration.ofMillis(millis));
    }

    public static String durationAsString(Duration duration) {
        return String.format("%02d:%02d:%02d.%03d", duration.toHours(),
                duration.toMinutes() % 60L,
                duration.getSeconds() % 60L,
                duration.getNano() / 1000000 % 1000);
    }

    public static long computeExponentialWaitTimeInMillis(int iteration, long initialWaitTime, long maxWaitTime) {
        if (initialWaitTime < 0L) {
            return 0L;
        }
        if (iteration <= 0) {
            return initialWaitTime;
        }
        if (maxWaitTime < initialWaitTime) {
            throw new IllegalArgumentException("The maximum wait time must be larger than the initial time");
        }

        //64 Max beacuse shifting can max go 64bits
        if (iteration >= 64) {
            return maxWaitTime;
        }
        if (initialWaitTime == 0L) {
            initialWaitTime = 1L;
        }
        //Shift to in increase wait time
        long result = initialWaitTime << iteration;
        return (result <= 0L) ? maxWaitTime : Math.min(maxWaitTime, result);
    }
}
