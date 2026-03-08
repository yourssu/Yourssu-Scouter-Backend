package com.yourssu.scouter.common.application.domain.authentication

import com.yourssu.scouter.common.business.domain.authentication.OAuth2RefreshTokenCheckResult

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
