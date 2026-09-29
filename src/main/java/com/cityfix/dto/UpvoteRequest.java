package com.cityfix.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpvoteRequest(@NotBlank @Email String voterEmail) {
}
