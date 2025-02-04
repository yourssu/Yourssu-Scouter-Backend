package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class UserNotFoundException(
    message: String,
) : CustomException(message, "User-001", HttpStatus.NOT_FOUND)
