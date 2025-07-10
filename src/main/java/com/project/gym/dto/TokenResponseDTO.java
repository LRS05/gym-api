package com.project.gym.dto;

public record TokenResponseDTO(

        String accessToken,

        String refreshToken
) {
}
