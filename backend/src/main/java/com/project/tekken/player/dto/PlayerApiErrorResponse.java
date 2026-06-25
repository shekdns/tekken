package com.project.tekken.player.dto;

public record PlayerApiErrorResponse(
        String code,
        String message,
        int upstreamStatus,
        String path
) {
}
