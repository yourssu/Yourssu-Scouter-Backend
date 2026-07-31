package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class AssignedQuestionInvalidException(
    message: String,
) : CustomException(message, "Question-004", HttpStatus.BAD_REQUEST)
