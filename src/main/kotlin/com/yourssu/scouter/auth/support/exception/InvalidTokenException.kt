package com.yourssu.scouter.auth.support.exception

import com.yourssu.scouter.common.exception.CustomException

import org.springframework.http.HttpStatus

class InvalidTokenException(
    message: String
) : CustomException(message, "Auth-001", HttpStatus.UNAUTHORIZED)
