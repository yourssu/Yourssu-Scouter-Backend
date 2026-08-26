package com.yourssu.scouter.auth.authentication.application.dto

import jakarta.validation.constraints.NotBlank

data class WithdrawalRequest(

    @field:NotBlank(message = "refresh token이 입력되지 않았습니다.")
    val refreshToken: String,
)
