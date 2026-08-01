package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class QuestionNotFoundException(
    message: String,
) : CustomException(message, "Question-005", HttpStatus.NOT_FOUND)
