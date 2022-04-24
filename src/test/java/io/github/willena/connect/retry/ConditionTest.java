package io.github.willena.connect.retry;

import io.github.willena.connect.counter.RetryTimeoutExceeded;
import org.apache.kafka.connect.errors.ConnectException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConditionTest {

    @Test
    void or() {
        Condition c = Condition.never()
                .or(p -> p.getMessage().contains("toto"))
                .or(p -> p.getMessage().contains("tutu"));

        assertTrue(c.isTrue(new Exception("toto")));
        assertTrue(c.isTrue(new Exception("tutu")));
        assertFalse(c.isTrue(new Exception("Nope")));
    }

    @Test
    void and() {
        Condition c = p -> p.getMessage().contains("toto");
        c = c.and(p -> p.getMessage().contains("tutu"));

        assertFalse(c.isTrue(new Exception("toto")));
        assertFalse(c.isTrue(new Exception("tutu")));
        assertTrue(c.isTrue(new Exception("tututoto")));
    }

    @Test
    void always() {
        Condition c = Condition.always().and(p -> p.getMessage().contains("nope"));

        assertTrue(c.isTrue(new Exception("nope")));
    }

    @Test
    void never() {
        Condition c = Condition.never().and(p -> p.getMessage().contains("nope"));

        assertFalse(c.isTrue(new Exception("nope")));
    }

    @Test
    void exceptionCondition() {
        assertTrue(Condition.isInstance(ConnectException.class).isTrue(new ConnectException("Error")));
        assertTrue(Condition.isInstance(ConnectException.class, IllegalArgumentException.class).isTrue(new IllegalArgumentException("Error")));
        assertFalse(Condition.isInstance(RetryTimeoutExceeded.class).isTrue(new IllegalArgumentException("III")));
        assertThrows(IllegalArgumentException.class, () -> Condition.isInstance().isTrue(new Exception("jjj")));
    }

}