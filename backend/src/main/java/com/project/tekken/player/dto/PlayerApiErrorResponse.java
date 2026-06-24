package com.project.tekken.player.dto;

public record PlayerApiErrorResponse(
        String message,
        int upstreamStatus,
        String path
) {
}
