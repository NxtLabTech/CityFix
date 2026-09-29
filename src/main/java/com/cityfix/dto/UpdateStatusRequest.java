package com.cityfix.dto;

import com.cityfix.model.ReportStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull ReportStatus status) {
}
