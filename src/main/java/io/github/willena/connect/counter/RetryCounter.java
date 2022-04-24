package io.github.willena.connect.counter;

import io.github.willena.connect.TimeUtil;
import io.github.willena.connect.backoff.BackoffInterruptedException;
import io.github.willena.connect.backoff.BackoffTimer;
import org.apache.kafka.common.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class RetryCounter implements AutoCloseable {
    protected static final long SLEEP_INCREMENT = TimeUnit.SECONDS.toMillis(2L);
    private final Logger LOGGER = LoggerFactory.getLogger(RetryCounter.class);
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Time clock;
    private final int minRetries;
    private final int maxRetries;
    private final long maxTotalRetryTimeoutMillis;
    private final BackoffTimer backoffTimer;
    private final String name = "";
    private long stopTimeMillis;
    private int retryAttempts;

    protected RetryCounter(int minRetries, int maxRetries, long maxTotalRetryTimeoutMillis, BackoffTimer backoffTimer, Time clock) {

        if (minRetries < 0) {
            throw new IllegalArgumentException("The minimum number of retries must be 0 or more");
        }
        if (maxRetries < minRetries) {
            throw new IllegalArgumentException("The maximum number of retries (" + maxRetries + ") must be larger than the minimum number (" + minRetries + ")");
        }
        if (maxTotalRetryTimeoutMillis <= 0L) {
            throw new IllegalArgumentException("The maximum retry timeout must be positive");
        }
        if (backoffTimer == null) {
            throw new IllegalArgumentException("The backoff policy may not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("The clock may not be null");
        }
        this.minRetries = minRetries;
        this.maxRetries = maxRetries;
        this.backoffTimer = backoffTimer;
        this.clock = clock;
        this.maxTotalRetryTimeoutMillis = maxTotalRetryTimeoutMillis;
        this.stopTimeMillis = -1L;
    }

    public static RetryCounter using(int minRetries, int maxRetries, long maxTotalRetryTimeout, TimeUnit unit, BackoffTimer timer) {
        return new RetryCounter(minRetries, maxRetries, unit.toMillis(maxTotalRetryTimeout), timer, Time.SYSTEM);
    }

    public static RetryCounter using(int minRetries, int maxRetries, long maxTotalRetryTimeout, TimeUnit unit, BackoffTimer timer, Time clock) {
        return new RetryCounter(minRetries, maxRetries, unit.toMillis(maxTotalRetryTimeout), timer, clock);
    }

    public boolean backoffAfterFailedAttempt(String reason) throws RetryCountExceeded, BackoffInterruptedException, RetryTimeoutExceeded {
        if (this.retryAttempts < this.minRetries) {
            this.retryAttempts++;
            this.LOGGER.debug("Retry {} of {} before beginning {} backoff", this.retryAttempts, this.minRetries, type());
            return false;
        }
        if (this.retryAttempts >= this.maxRetries) {
            throw new RetryCountExceeded(String.format("Exceeded the maximum number of retries (%s)", maxRetries), this.maxRetries, null);
        }


        long timeRemaining = timeRemaining();
        if (timeRemaining <= 0L) {
            throw new RetryTimeoutExceeded(String.format(" Exceeded the maximum retry time (%s)", this.maxTotalRetryTimeoutMillis), Duration.ofMillis(maxTotalRetryTimeoutMillis), null);
        }


        long sleepTimeMs = this.backoffTimer.computeBackoffMillis(this.retryAttempts - this.minRetries);
        sleepTimeMs = Math.min(sleepTimeMs, timeRemaining);
        String sleepTimeStr = TimeUtil.durationAsString(sleepTimeMs);
        if (reason == null || reason.isEmpty()) {
            this.LOGGER.debug("Start {} backoff of {} before another attempt", type(), sleepTimeStr);
        } else {
            this.LOGGER.debug("Start {} backoff of {} before another attempt: {}", type(), sleepTimeStr, reason);
        }


        long startMillis = this.clock.milliseconds();
        if (sleep(sleepTimeMs)) {
            this.LOGGER.debug("Completed {} backoff of {}", type(), sleepTimeStr);
        } else {
            long elapsedMillis = this.clock.milliseconds() - startMillis;
            throw new BackoffInterruptedException("", Duration.ofMillis(elapsedMillis), null);
        }
        this.retryAttempts++;
        return true;
    }


    public void close() {
        this.running.set(false);
    }

    protected String type() {
        return this.backoffTimer.type();
    }

    protected boolean sleep(long timeInMillis) {
        long remainingMillis = timeInMillis;
        long stopMillis = this.clock.milliseconds() + timeInMillis;
        while (this.running.get() && remainingMillis > 0L) {
            long sleepTimeMs = Math.min(remainingMillis, SLEEP_INCREMENT);
            if (this.LOGGER.isTraceEnabled()) {
                this.LOGGER.trace("Sleeping for {}ms, {}ms remaining",

                        TimeUtil.durationAsString(sleepTimeMs), remainingMillis);
            }

            this.clock.sleep(Math.min(remainingMillis, SLEEP_INCREMENT));
            remainingMillis = stopMillis - this.clock.milliseconds();
        }
        return this.running.get();
    }


    protected long timeRemaining() {
        if (this.maxTotalRetryTimeoutMillis == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        long now = this.clock.milliseconds();
        if (this.stopTimeMillis == -1L) {
            this.stopTimeMillis = now + this.maxTotalRetryTimeoutMillis;
            if (this.stopTimeMillis < 0L) {

                this.stopTimeMillis = Long.MAX_VALUE;
                return this.stopTimeMillis;
            }
        }
        return this.stopTimeMillis - now;
    }

    public int retries() {
        return this.retryAttempts;
    }

    public int maxRetries() {
        return maxRetries;
    }
}

