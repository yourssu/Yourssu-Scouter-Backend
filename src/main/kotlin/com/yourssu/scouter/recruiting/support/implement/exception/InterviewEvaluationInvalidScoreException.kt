package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class InterviewEvaluationInvalidScoreException(
    message: String,
) : CustomException(message, "InterviewEvaluation-001", HttpStatus.BAD_REQUEST)
