package com.logistics.hub.common.exception;

public class GeocodingException extends RuntimeException {
    
    public GeocodingException(String message) {
        super(message);
    }
    
    public GeocodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
