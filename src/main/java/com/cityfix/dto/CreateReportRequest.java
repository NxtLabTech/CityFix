package com.cityfix.dto;

import com.cityfix.model.Category;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReportRequest(
        @NotBlank @Size(max = 255, message = "Title must not exceed 255 characters") String title,
        @NotBlank @Size(max = 2000, message = "Description must not exceed 2000 characters") String description,
        @NotNull Category category,
        @NotBlank @Size(max = 255, message = "Area must not exceed 255 characters") String area,
        @Size(max = 255, message = "Address must not exceed 255 characters") String address,
        @NotBlank @Size(max = 255, message = "Reporter name must not exceed 255 characters") String reporterName,
        @NotBlank @Email @Size(max = 255, message = "Reporter email must not exceed 255 characters") String reporterEmail) {
}