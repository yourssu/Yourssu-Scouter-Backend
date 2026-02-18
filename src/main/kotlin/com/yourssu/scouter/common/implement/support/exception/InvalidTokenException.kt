package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class InvalidTokenException(
    message: String
) : CustomException(message, "Auth-001", HttpStatus.UNAUTHORIZED)
