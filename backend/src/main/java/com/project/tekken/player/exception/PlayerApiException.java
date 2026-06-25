package com.project.tekken.player.exception;

import org.springframework.http.HttpStatusCode;

public class PlayerApiException extends RuntimeException {

    private final HttpStatusCode statusCode;
    private final String code;

    public PlayerApiException(HttpStatusCode statusCode, String message) {
        this(statusCode, "PLAYER_API_ERROR", message);
    }

    public PlayerApiException(HttpStatusCode statusCode, String code, String message) {
        super(message);
        this.statusCode = statusCode;
        this.code = code;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getCode() {
        return code;
    }
}
