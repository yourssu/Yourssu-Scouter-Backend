package com.yourssu.scouter.common.application.domain.authentication

import jakarta.validation.constraints.NotBlank

data class OAuth2LoginRequest(

    @field:NotBlank(message = "authorization code가 입력되지 않았습니다.")
    val authorizationCode: String,
)
