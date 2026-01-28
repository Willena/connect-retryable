[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.willena/connect-retryable/badge.svg)](https://search.maven.org/artifact/io.github.willena/connect-retryable/)

# Deprecated / Archived

Use something like resilience4j (https://github.com/resilience4j/resilience4j) instead. 

# Connect Retryable

A simple library to handle multiple retries in case of Exceptions in Kafka Connectors (Sink)

It provides:

- Retry Count
- Retry with backoff
  - Constant
  - Exponential
  - No backoff
- Retry timeout

## Simple Example

```java

import io.github.willena.connect.backoff.BackoffTimers;
import io.github.willena.connect.retry.Condition;
import io.github.willena.connect.retry.Retryable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class MyApp {

    public MyApp() {

        Retryable r = Retryable.builder()
                .withMaxRetries(10)
                .withBackoffTimer(BackoffTimers.noBackoff())
                .withMaxRetryTimeout(Duration.of(1, ChronoUnit.DAYS))
                .when(Condition.isInstance(Exception.class)).build();

        // call can throws RetryCountExceeded, BackoffInterruptedException, RetryTimeoutExceeded
        // if function does not succeed in time...
        r.call("Call my database", () -> {
            // Do something that can return an exception
            Integer.parseInt("Not and int ");
        });

    }

}

```
