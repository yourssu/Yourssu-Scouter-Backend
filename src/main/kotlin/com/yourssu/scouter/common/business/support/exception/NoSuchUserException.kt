package com.yourssu.scouter.common.business.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class NoSuchUserException(
    message: String,
) : CustomException(message, "Auth-004", HttpStatus.UNAUTHORIZED)
