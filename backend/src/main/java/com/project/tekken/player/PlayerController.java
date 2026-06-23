package com.project.tekken.player;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/{tekkenId}")
    public PlayerProfileResponse getProfile(@PathVariable @NotBlank String tekkenId) {
        return playerService.getProfile(tekkenId);
    }

    @GetMapping("/{tekkenId}/matches")
    public PlayerMatchesResponse getMatches(@PathVariable @NotBlank String tekkenId) {
        return playerService.getMatches(tekkenId);
    }

    @ExceptionHandler(PlayerApiException.class)
    public ResponseEntity<PlayerApiErrorResponse> handlePlayerApiException(PlayerApiException exception) {
        return ResponseEntity
                .status(exception.getStatusCode())
                .body(new PlayerApiErrorResponse(
                        exception.getMessage(),
                        exception.getStatusCode().value(),
                        "/api/players"));
    }
}
