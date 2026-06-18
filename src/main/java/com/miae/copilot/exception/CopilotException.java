package com.miae.copilot.exception;

public class CopilotException extends RuntimeException {

    public CopilotException(String message) {
        super(message);
    }

    public CopilotException(String message, Throwable cause) {
        super(message, cause);
    }
}
