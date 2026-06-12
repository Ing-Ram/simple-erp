package com.simpleerp.shared;

import java.util.List;
import org.springframework.data.domain.Page;

/** Stable, framework-agnostic paging envelope so the client never depends on Spring's Page shape. */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    /** Wraps an already-mapped page of DTOs. */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
