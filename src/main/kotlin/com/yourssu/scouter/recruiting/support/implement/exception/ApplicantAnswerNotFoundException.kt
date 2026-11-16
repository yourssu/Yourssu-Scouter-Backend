package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class ApplicantAnswerNotFoundException(
    message: String,
) : CustomException(message, "Applicant-003", HttpStatus.NOT_FOUND)
