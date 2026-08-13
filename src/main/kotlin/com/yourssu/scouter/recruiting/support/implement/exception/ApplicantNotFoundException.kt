package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class ApplicantNotFoundException(
    message: String,
) : CustomException(message, "Applicant-001", HttpStatus.NOT_FOUND)
