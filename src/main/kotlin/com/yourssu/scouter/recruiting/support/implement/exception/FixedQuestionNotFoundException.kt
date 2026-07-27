package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class FixedQuestionNotFoundException(
    message: String,
) : CustomException(message, "Question-001", HttpStatus.NOT_FOUND)
