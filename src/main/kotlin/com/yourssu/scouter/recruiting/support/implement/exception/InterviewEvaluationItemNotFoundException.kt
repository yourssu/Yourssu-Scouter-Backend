package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class InterviewEvaluationItemNotFoundException(
    message: String,
) : CustomException(message, "InterviewEvaluation-002", HttpStatus.NOT_FOUND)
