package com.project.tekken.player;

import org.springframework.http.HttpStatusCode;

public class PlayerApiException extends RuntimeException {

    private final HttpStatusCode statusCode;

    public PlayerApiException(HttpStatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
