package com.yourssu.scouter.common.support.application.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class LoginRequiredException(
    message: String,
) : CustomException(message, "Auth-003", HttpStatus.UNAUTHORIZED)
