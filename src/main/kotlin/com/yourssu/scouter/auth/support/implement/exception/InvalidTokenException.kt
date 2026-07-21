package com.yourssu.scouter.auth.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException

import org.springframework.http.HttpStatus

class InvalidTokenException(
    message: String
) : CustomException(message, "Auth-001", HttpStatus.UNAUTHORIZED)
