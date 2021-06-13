package io.ulbrich.imageservice.exception;

public class UnsupportedImageException extends RuntimeException {
    private String detectedType;

    public UnsupportedImageException(String detectedType) {
        this.detectedType = detectedType;
    }

    public UnsupportedImageException(Throwable cause, String detectedType) {
        super(cause);
        this.detectedType = detectedType;
    }

    public String getDetectedType() {
        return detectedType;
    }

    public void setDetectedType(String detectedType) {
        this.detectedType = detectedType;
    }
}
