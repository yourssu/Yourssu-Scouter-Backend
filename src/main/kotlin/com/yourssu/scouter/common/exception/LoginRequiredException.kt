package com.yourssu.scouter.common.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class LoginRequiredException(
    message: String,
) : CustomException(message, "Auth-003", HttpStatus.UNAUTHORIZED)
