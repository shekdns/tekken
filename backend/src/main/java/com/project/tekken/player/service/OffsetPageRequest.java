package com.project.tekken.player.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

final class OffsetPageRequest implements Pageable {

    private final int limit;
    private final long offset;

    OffsetPageRequest(int limit, long offset) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be negative");
        }
        this.limit = limit;
        this.offset = offset;
    }

    @Override
    public int getPageNumber() {
        return Math.toIntExact(offset / limit);
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return Sort.unsorted();
    }

    @Override
    public Pageable next() {
        return new OffsetPageRequest(limit, offset + limit);
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? new OffsetPageRequest(limit, Math.max(offset - limit, 0)) : first();
    }

    @Override
    public Pageable first() {
        return new OffsetPageRequest(limit, 0);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number must not be negative");
        }
        return new OffsetPageRequest(limit, (long) pageNumber * limit);
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }
}
