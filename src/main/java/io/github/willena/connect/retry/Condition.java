package io.github.willena.connect.retry;

import java.util.Arrays;
import java.util.Objects;

@FunctionalInterface
public interface Condition {
    static Condition always() {
        return e -> true;
    }

    static Condition never() {
        return e -> false;
    }

    @SafeVarargs
    static Condition isInstance(Class<? extends Exception>... errorClasses) {
        if (errorClasses.length == 0) {
            throw new IllegalArgumentException("At least one exception class must be specified");
        }
        return e -> Arrays.stream(errorClasses).anyMatch(aClass -> aClass.isInstance(e));
    }

    default Condition or(Condition other) {
        Objects.requireNonNull(other);
        return e -> (isTrue(e) || other.isTrue(e));
    }

    default Condition and(Condition other) {
        Objects.requireNonNull(other);
        return e -> (isTrue(e) && other.isTrue(e));
    }

    boolean isTrue(Exception paramException);
}
