package com.yourssu.scouter.mail.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException

import org.springframework.http.HttpStatus

class MailFileNotFoundException(
    message: String,
) : CustomException(message, "MailFile-001", HttpStatus.NOT_FOUND)
