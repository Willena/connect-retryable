package io.github.willena.connect.retry;

import io.github.willena.connect.backoff.BackoffInterruptedException;
import io.github.willena.connect.backoff.BackoffTimer;
import io.github.willena.connect.backoff.BackoffTimers;
import io.github.willena.connect.counter.RetryCountExceeded;
import io.github.willena.connect.counter.RetryCounter;
import io.github.willena.connect.counter.RetryTimeoutExceeded;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.connect.errors.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class Retryable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Retryable.class);

    private static final Duration MAX_DURATION = Duration.ofMillis(Long.MAX_VALUE);

    private final int maxAttempts;

    private final Condition errorCondition;

    private final BackoffTimer backoffTimer;

    private final Optional<Duration> maxRetryTimeout;

    private final Time clock;


    protected Retryable(Builder builder) {
        this.maxAttempts = builder.maxAttempts;
        this.errorCondition = builder.whenError.orElse(Condition.never());
        this.backoffTimer = builder.backoffPolicy.orElse(BackoffTimers.noBackoff());
        this.maxRetryTimeout = builder.maxRetryTimeout;
        this.clock = builder.clock.orElse(Time.SYSTEM);
        assert this.maxAttempts >= 1;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int maxAttempts() {
        return this.maxAttempts;
    }

    public int maxRetries() {
        return this.maxAttempts - 1;
    }

    public Optional<Duration> maxRetryTimeout() {
        return this.maxRetryTimeout;
    }

    public BackoffTimer backoffTimer() {
        return this.backoffTimer;
    }

    public Condition retryCondition() {
        return this.errorCondition;
    }

    public <T> T call(String description, Callable<T> function) throws RetryCountExceeded, BackoffInterruptedException, RetryTimeoutExceeded {
        return callWith(description, () -> null, nullResource -> function.call());
    }

    public <ResourceT extends AutoCloseable, T> T callWith(String description, ResourceSupplier<ResourceT> resourceSupplier, FunctionWithResource<ResourceT, T> function) throws RetryCountExceeded, BackoffInterruptedException, RetryTimeoutExceeded {
        Exception lastException;
        try (RetryCounter retryCounter = newRetryCounter()) {
            while (true) {
                LOGGER.trace("Create resources for {} (attempt {} of {})", description, retryCounter.retries() + 1, retryCounter.maxRetries() + 1);

                try (ResourceT autoCloseable = resourceSupplier.get()) {
                    LOGGER.trace("Try {} (attempt {} of {})", description, retryCounter.retries() + 1, retryCounter.maxRetries() + 1);
                    return function.apply(autoCloseable);
                } catch (Exception e) {
                    lastException = e;

                    if (!this.errorCondition.isTrue(e)) {
                        throw new ConnectException(String.format("Operation: %s; Failed with exception %s is not marked as retryable. Failed at 1st attempt. Message: %s", description, lastException.getClass().getSimpleName(), lastException.getMessage()));
                    }

                    LOGGER.trace("Waiting before retrying to {} (attempt {} of {})", description, retryCounter.retries() + 1, retryCounter.maxRetries() + 1);
                    retryCounter.backoffAfterFailedAttempt(String.valueOf(e));
                    LOGGER.debug("Retrying to {} (attempt {} of {}) after previous retriable error: {}", description, retryCounter.retries() + 1, retryCounter.maxRetries() + 1, e.getMessage(), e);

                }
            }
        }

    }

    public String toString() {
        if (maxRetryTimeout().isPresent()) {
            return String.format("Call functions and retry up to %d times or as many times up to %d milliseconds using %s backoff", maxRetries(), maxRetryTimeout().get().toMillis(), backoffTimer().type());
        }
        return String.format("Call functions and retry up to %d times using %s backoff", maxRetries(), backoffTimer().type());
    }

    protected RetryCounter newRetryCounter() {
        return RetryCounter.using(0, maxRetries(), this.maxRetryTimeout.orElse(MAX_DURATION).toMillis(), TimeUnit.MILLISECONDS, backoffTimer(), this.clock);
    }

//    protected String interruptedExceptionMessageFor(Exception lastException, String operationDescription, int attempt, Duration interruptedAfter) {
//        return String.format("Interrupted after %s on attempt %d of %d to %s. Previous error: %s", interruptedAfter.toString(), attempt, this.maxAttempts, operationDescription, lastException.getMessage());
//    }
//
//    protected String exceptionMessageFor(Exception lastException, String operationDescription, int attempt, int maxAttempts) {
//        if (maxAttempts == 1 || attempt == 1) {
//            return String.format("Failed on 1st attempt to %s: %s", operationDescription, lastException.getMessage());
//        }
//
//        if (attempt == maxAttempts) {
//            return String.format("Failed after %d attempts to %s: %s", maxAttempts, operationDescription, lastException.getMessage());
//        }
//        return String.format("Failed on attempt %d of %d to %s: %s", attempt, maxAttempts, operationDescription, lastException.getMessage());
//    }

    public static class Builder {
        private final Optional<Time> clock = Optional.empty();
        private int maxAttempts = 1;
        private Optional<Condition> whenError = Optional.empty();
        private Optional<BackoffTimer> backoffPolicy = Optional.empty();
        private Optional<Duration> maxRetryTimeout = Optional.empty();

        private Builder withMaxAttempts(int maxAttempts) {
            if (maxAttempts < 1) {
                throw new IllegalArgumentException(String.format("Maximum number of attempts (%d) must be positive.", maxAttempts));
            }

            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder withMaxRetries(int maxRetries) {
            return withMaxAttempts(1 + maxRetries);
        }

        public Builder withMaxRetryTimeout(Duration maxRetryTimeout) {
            this.maxRetryTimeout = Optional.ofNullable(maxRetryTimeout);
            return this;
        }

        public Builder withBackoffTimer(BackoffTimer backoffTimer) {
            this.backoffPolicy = Optional.ofNullable(backoffTimer);
            return this;
        }

        public Builder when(Condition retryWhen) {
            Objects.requireNonNull(retryWhen);
            if (this.maxAttempts <= 1) {
                throw new IllegalArgumentException("The retry condition will never be called since maxAttempts=1");
            }

            this.whenError = Optional.of(retryWhen);
            return this;
        }

        public Retryable build() {
            return new Retryable(this);
        }
    }

}

