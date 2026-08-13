package com.yourssu.scouter.mail.file.implement

import com.yourssu.scouter.common.exception.CustomException

import org.springframework.http.HttpStatus

class MailFileInvalidUsageException(
    message: String,
) : CustomException(message, "MailFile-004", HttpStatus.BAD_REQUEST)
