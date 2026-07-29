package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class InterviewRubricNotFoundException(
    message: String,
) : CustomException(message, "InterviewRubric-001", HttpStatus.NOT_FOUND)
