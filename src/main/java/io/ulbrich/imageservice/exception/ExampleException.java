package io.ulbrich.imageservice.exception;

public class ExampleException extends RuntimeException {
    private final String dummy;

    public ExampleException(String dummy) {
        this.dummy = dummy;
    }

    public ExampleException(Throwable cause, String dummy) {
        super(cause);
        this.dummy = dummy;
    }

    public String getDummy() {
        return dummy;
    }
}
