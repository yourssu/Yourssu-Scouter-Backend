package com.yourssu.scouter.common.support.business.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class NoSuchUserException(
    message: String,
) : CustomException(message, "Auth-004", HttpStatus.UNAUTHORIZED)
