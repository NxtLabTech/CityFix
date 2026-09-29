package com.cityfix.mapper;

import com.cityfix.dto.ReportResponse;
import com.cityfix.model.Report;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ReportMapper {

    public ReportResponse toDTO(Report report) {
        if (report == null) {
            return null;
        }

        return new ReportResponse(
                report.getId(),
                report.getTitle(),
                report.getDescription(),
                report.getArea(),
                report.getAddress(),
                report.getStatus(),
                report.getReporterName(),
                report.getUpvoteCount(),
                report.getCategory(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                report.getFixedAt()
        );
    }

    public List<ReportResponse> toDTO(List<Report> reports) {
        if (reports == null || reports.isEmpty()) {
            return Collections.emptyList();
        }

        return reports.stream()
                .map(this::toDTO)
                .toList();
    }
}