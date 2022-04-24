package io.github.willena.connect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RetryExceptionTest {

    @Test
    void retryConstructor() {
        assertEquals("Could not rery", new RetryException("Could not rery", null).getMessage());
    }

}