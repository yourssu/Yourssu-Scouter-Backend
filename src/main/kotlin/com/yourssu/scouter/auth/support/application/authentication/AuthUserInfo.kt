package com.yourssu.scouter.auth.support.application.authentication

import io.swagger.v3.oas.annotations.Hidden

@Hidden // AuthUserInfoArgumentResolver 에서 처리함
data class AuthUserInfo(
    val userId: Long,
)
