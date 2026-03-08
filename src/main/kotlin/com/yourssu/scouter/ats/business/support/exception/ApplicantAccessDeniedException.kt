package com.yourssu.scouter.ats.business.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class ApplicantAccessDeniedException(
    message: String,
) : CustomException(message, "Applicant-002", HttpStatus.FORBIDDEN)
