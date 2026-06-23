package com.project.tekken.search;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "player_search_history")
public class PlayerSearchHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "query", nullable = false)
    private String query;

    @Column(name = "tekken_id", length = 32)
    private String tekkenId;

    @Column(name = "searched_at", nullable = false)
    private Instant searchedAt;

    protected PlayerSearchHistoryEntity() {
    }

    public PlayerSearchHistoryEntity(String query, String tekkenId, Instant searchedAt) {
        this.query = query;
        this.tekkenId = tekkenId;
        this.searchedAt = searchedAt;
    }

    public Long getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public String getTekkenId() {
        return tekkenId;
    }

    public Instant getSearchedAt() {
        return searchedAt;
    }
}
