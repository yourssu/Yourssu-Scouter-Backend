package com.yourssu.scouter.document.implement.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class RubricLockedException(
    message: String,
) : CustomException(message, "DocumentSection-002", HttpStatus.CONFLICT)
