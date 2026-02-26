package com.yourssu.scouter.common.business.domain.authentication

data class OAuth2RefreshTokenCheckResult(
    val valid: Boolean,
    val errorCode: String?,
)
