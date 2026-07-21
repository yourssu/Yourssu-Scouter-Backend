package com.yourssu.scouter.auth.authentication.application.dto

import com.yourssu.scouter.auth.authentication.business.dto.OAuth2RefreshTokenCheckResult

data class OAuth2RefreshTokenStatusResponse(
    val valid: Boolean,
    val errorCode: String? = null,
) {
    companion object {
        fun from(result: OAuth2RefreshTokenCheckResult): OAuth2RefreshTokenStatusResponse {
            return OAuth2RefreshTokenStatusResponse(
                valid = result.valid,
                errorCode = result.errorCode,
            )
        }
    }
}
