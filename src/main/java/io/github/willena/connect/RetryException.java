package io.github.willena.connect;

import org.apache.kafka.connect.errors.ConnectException;


public class RetryException extends ConnectException {
    public RetryException(String msg, Exception lastException) {
        super(msg, lastException);
    }
}


