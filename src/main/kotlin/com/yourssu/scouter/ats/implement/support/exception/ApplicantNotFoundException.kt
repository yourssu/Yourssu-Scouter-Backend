package com.yourssu.scouter.ats.implement.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class ApplicantNotFoundException(
    message: String,
) : CustomException(message, "Applicant-001", HttpStatus.NOT_FOUND)
