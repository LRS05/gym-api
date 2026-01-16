package com.project.gym.dto;

import com.project.gym.entity.enums.ApiError;

import java.time.LocalDateTime;

public record ErrorResponseDTO(

        LocalDateTime timestamp,
        int status,
        String message,
        ApiError error,
        String path
) {
}
