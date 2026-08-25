package com.yourssu.scouter.auth.support.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class InvalidCredentialsException(
    message: String,
) : CustomException(message, "Auth-006", HttpStatus.UNAUTHORIZED)
