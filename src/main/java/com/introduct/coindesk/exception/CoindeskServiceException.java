package com.introduct.coindesk.exception;

// TODO make exception model better
public class CoindeskServiceException extends RuntimeException {

    public CoindeskServiceException(String message) {
        super(message);
    }
    public CoindeskServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
