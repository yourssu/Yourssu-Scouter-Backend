package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class QuestionnaireGroupInvalidException(
    message: String,
) : CustomException(message, "Question-002", HttpStatus.BAD_REQUEST)
