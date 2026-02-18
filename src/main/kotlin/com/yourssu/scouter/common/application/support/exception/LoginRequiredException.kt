package com.yourssu.scouter.common.application.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class LoginRequiredException(
    message: String,
) : CustomException(message, "Auth-003", HttpStatus.UNAUTHORIZED)
