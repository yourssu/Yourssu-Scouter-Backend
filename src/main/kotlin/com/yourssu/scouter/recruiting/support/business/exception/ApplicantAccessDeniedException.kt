package com.yourssu.scouter.recruiting.support.business.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class ApplicantAccessDeniedException(
    message: String,
) : CustomException(message, "Applicant-002", HttpStatus.FORBIDDEN)
