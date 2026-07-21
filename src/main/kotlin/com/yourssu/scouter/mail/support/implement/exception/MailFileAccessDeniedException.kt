package com.yourssu.scouter.mail.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException

import org.springframework.http.HttpStatus

class MailFileAccessDeniedException(
    message: String,
) : CustomException(message, "MailFile-002", HttpStatus.FORBIDDEN)
