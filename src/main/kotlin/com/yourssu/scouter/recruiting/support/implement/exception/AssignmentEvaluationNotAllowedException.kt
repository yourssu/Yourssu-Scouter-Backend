package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class AssignmentEvaluationNotAllowedException(
    message: String,
) : CustomException(message, "Assignment-001", HttpStatus.BAD_REQUEST)
