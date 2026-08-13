package com.yourssu.scouter.common.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class NoSuchUserException(
    message: String,
) : CustomException(message, "Auth-004", HttpStatus.UNAUTHORIZED)
