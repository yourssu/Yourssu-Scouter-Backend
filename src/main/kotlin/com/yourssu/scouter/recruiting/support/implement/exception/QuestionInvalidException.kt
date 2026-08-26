package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class QuestionInvalidException(
    message: String,
) : CustomException(message, "Question-006", HttpStatus.BAD_REQUEST)
