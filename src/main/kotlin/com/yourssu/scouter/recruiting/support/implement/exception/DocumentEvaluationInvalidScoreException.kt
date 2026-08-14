package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class DocumentEvaluationInvalidScoreException(
    message: String,
) : CustomException(message, "DocumentEvaluation-001", HttpStatus.BAD_REQUEST)
