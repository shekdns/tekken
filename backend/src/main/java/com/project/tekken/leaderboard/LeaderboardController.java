package com.project.tekken.leaderboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaderboards")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/players")
    public PlayerLeaderboardResponse players(
            @RequestParam(defaultValue = "prowess") String sort,
            @RequestParam(required = false) String character,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String platform,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return leaderboardService.players(new LeaderboardFilters(sort, character, region, platform, limit));
    }
}
