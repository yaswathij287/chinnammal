package com.smarthostel.exception;

public class HostelException extends Exception {
    public HostelException(String message) {
        super(message);
    }

    public HostelException(String message, Throwable cause) {
        super(message, cause);
    }
}
