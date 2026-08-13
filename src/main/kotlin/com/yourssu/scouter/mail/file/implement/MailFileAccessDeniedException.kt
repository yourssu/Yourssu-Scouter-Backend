package com.yourssu.scouter.mail.file.implement

import com.yourssu.scouter.common.exception.CustomException

import org.springframework.http.HttpStatus

class MailFileAccessDeniedException(
    message: String,
) : CustomException(message, "MailFile-002", HttpStatus.FORBIDDEN)
