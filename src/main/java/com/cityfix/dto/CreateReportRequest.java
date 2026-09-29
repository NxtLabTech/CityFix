package com.cityfix.dto;

import com.cityfix.model.Category;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateReportRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull Category category,
        @NotBlank String area,
        String address,
        @NotBlank String reporterName,
        @NotBlank @Email String reporterEmail) {
}
