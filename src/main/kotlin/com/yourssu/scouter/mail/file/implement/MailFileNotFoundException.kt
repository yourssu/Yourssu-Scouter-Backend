package com.yourssu.scouter.mail.file.implement

import com.yourssu.scouter.common.exception.CustomException

import org.springframework.http.HttpStatus

class MailFileNotFoundException(
    message: String,
) : CustomException(message, "MailFile-001", HttpStatus.NOT_FOUND)
