package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class AssignedQuestionNotFoundException(
    message: String,
) : CustomException(message, "Question-003", HttpStatus.NOT_FOUND)
