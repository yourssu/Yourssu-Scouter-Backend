package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class RubricLockedException(
    message: String,
) : CustomException(message, "DocumentSection-002", HttpStatus.CONFLICT)
