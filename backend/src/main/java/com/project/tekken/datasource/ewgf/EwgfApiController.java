package com.project.tekken.datasource.ewgf;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/ewgf")
public class EwgfApiController {

    private final EwgfApiService ewgfApiService;

    public EwgfApiController(EwgfApiService ewgfApiService) {
        this.ewgfApiService = ewgfApiService;
    }

    @GetMapping("/battles/{tekkenId}")
    public ResponseEntity<String> getBattles(@PathVariable @NotBlank String tekkenId) {
        return ewgfApiService.getBattles(tekkenId);
    }

    @GetMapping("/profile/{tekkenId}")
    public ResponseEntity<String> getProfile(@PathVariable @NotBlank String tekkenId) {
        return ewgfApiService.getProfile(tekkenId);
    }

    @PostMapping("/profile")
    public ResponseEntity<String> getProfiles(@RequestBody @NotEmpty List<@NotBlank String> tekkenIds) {
        return ewgfApiService.getProfiles(tekkenIds);
    }
}
