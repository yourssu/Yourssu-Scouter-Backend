package com.yourssu.scouter.recruiting.support.business.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class ApplicantAccessDeniedException(
    message: String,
) : CustomException(message, "Applicant-002", HttpStatus.FORBIDDEN)
