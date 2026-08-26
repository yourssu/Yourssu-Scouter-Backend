package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class ApplicantWebhookUnauthorizedException(
    message: String,
) : CustomException(message, "ApplicantWebhook-001", HttpStatus.UNAUTHORIZED)
