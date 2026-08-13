package com.yourssu.scouter.auth.support.exception

import com.yourssu.scouter.common.exception.CustomException

import org.springframework.http.HttpStatus

class UserNotFoundException(
    message: String,
) : CustomException(message, "User-001", HttpStatus.NOT_FOUND)
