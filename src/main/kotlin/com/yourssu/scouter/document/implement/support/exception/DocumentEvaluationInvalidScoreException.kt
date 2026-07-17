package com.yourssu.scouter.document.implement.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class DocumentEvaluationInvalidScoreException(
    message: String,
) : CustomException(message, "DocumentEvaluation-001", HttpStatus.BAD_REQUEST)
