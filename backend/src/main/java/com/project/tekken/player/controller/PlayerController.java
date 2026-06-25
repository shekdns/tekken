package com.project.tekken.player.controller;

import jakarta.validation.constraints.NotBlank;
import com.project.tekken.player.dto.PlayerApiErrorResponse;
import com.project.tekken.player.dto.PlayerMatchesResponse;
import com.project.tekken.player.dto.PlayerProfileResponse;
import com.project.tekken.player.dto.PlayerStatsResponse;
import com.project.tekken.player.exception.PlayerApiException;
import com.project.tekken.player.service.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public PlayerProfileResponse getProfile(
            @PathVariable @NotBlank String tekkenId,
            @RequestParam(defaultValue = "false") boolean refresh
    ) {
        return playerService.getProfile(tekkenId, refresh);
    }

    @GetMapping("/{tekkenId}/matches")
    public PlayerMatchesResponse getMatches(
            @PathVariable @NotBlank String tekkenId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(required = false) String battleType,
            @RequestParam(required = false) String character,
            @RequestParam(required = false) String opponentCharacter,
            @RequestParam(required = false) Integer days,
            @RequestParam(defaultValue = "false") boolean refresh
    ) {
        return playerService.getMatches(tekkenId, offset, limit, battleType, character, opponentCharacter, days, refresh);
    }

    @GetMapping("/{tekkenId}/stats")
    public PlayerStatsResponse getStats(
            @PathVariable @NotBlank String tekkenId,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String battleType,
            @RequestParam(required = false) String character,
            @RequestParam(required = false) String opponentCharacter,
            @RequestParam(required = false) Integer days
    ) {
        return playerService.getStats(tekkenId, limit, battleType, character, opponentCharacter, days);
    }

    @ExceptionHandler(PlayerApiException.class)
    public ResponseEntity<PlayerApiErrorResponse> handlePlayerApiException(PlayerApiException exception) {
        return ResponseEntity
                .status(exception.getStatusCode())
                .body(new PlayerApiErrorResponse(
                        exception.getCode(),
                        exception.getMessage(),
                        exception.getStatusCode().value(),
                        "/api/players"));
    }
}
