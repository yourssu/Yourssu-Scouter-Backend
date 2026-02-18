package com.yourssu.scouter.common.application.domain.authentication

import jakarta.validation.constraints.NotBlank

data class OAuth2LoginRequest(

    @field:NotBlank(message = "authorization code가 입력되지 않았습니다.")
    val authorizationCode: String,
    // 프론트가 넘긴 redirect_uri를 신뢰(allowlist 검증 전제)
    val redirectUri: String? = null,
)
