package com.yourssu.scouter.mail.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException

import org.springframework.http.HttpStatus

class MailFileAlreadyUsedException(
    message: String,
) : CustomException(message, "MailFile-003", HttpStatus.CONFLICT)
