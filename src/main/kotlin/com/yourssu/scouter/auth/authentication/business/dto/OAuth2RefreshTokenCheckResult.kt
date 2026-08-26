package com.yourssu.scouter.auth.authentication.business.dto

data class OAuth2RefreshTokenCheckResult(
    val valid: Boolean,
    val errorCode: String?,
)
