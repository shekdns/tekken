package com.project.tekken.player;

public record PlayerApiErrorResponse(
        String message,
        int upstreamStatus,
        String path
) {
}
