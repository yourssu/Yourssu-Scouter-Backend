package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class InterviewEvaluationAccessDeniedException(
    message: String,
) : CustomException(message, "InterviewEvaluation-003", HttpStatus.FORBIDDEN)
