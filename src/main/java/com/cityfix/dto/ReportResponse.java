package com.cityfix.dto;

import com.cityfix.model.Category;
import com.cityfix.model.ReportStatus;

import java.time.LocalDateTime;

public record ReportResponse(
        Long id,

        String title,
        String description,
        String area,
        String address,
        ReportStatus status,
        String reporterName,
        int upvoteCount,
        Category category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime fixedAt
) {
}
