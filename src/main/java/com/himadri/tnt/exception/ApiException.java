package com.himadri.tnt.exception;

public class ApiException extends Exception {

    public ApiException(String message) {
        super(message);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }
}
