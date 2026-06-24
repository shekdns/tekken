package com.project.tekken.datasource.ewgf;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class EwgfApiService {

    private final EwgfApiClient ewgfApiClient;

    public EwgfApiService(EwgfApiClient ewgfApiClient) {
        this.ewgfApiClient = ewgfApiClient;
    }

    public ResponseEntity<String> getBattles(String tekkenId) {
        return ewgfApiClient.getBattles(tekkenId);
    }

    public ResponseEntity<String> getProfile(String tekkenId) {
        return ewgfApiClient.getProfile(tekkenId);
    }

    public ResponseEntity<String> getProfiles(List<String> tekkenIds) {
        return ewgfApiClient.getProfiles(tekkenIds);
    }
}
