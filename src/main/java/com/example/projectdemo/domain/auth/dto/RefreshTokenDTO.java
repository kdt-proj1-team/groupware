package com.example.projectdemo.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenDTO {
    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    private String refreshToken;
}