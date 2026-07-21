package com.yourssu.scouter.auth.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException

import org.springframework.http.HttpStatus

class UnsupportedOAuth2LoginException(
    message: String,
) : CustomException(message, "Auth-002", HttpStatus.BAD_REQUEST)
