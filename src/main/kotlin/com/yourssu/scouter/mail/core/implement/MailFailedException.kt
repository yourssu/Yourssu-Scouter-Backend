package com.yourssu.scouter.mail.core.implement

import com.yourssu.scouter.common.exception.CustomException

import org.springframework.http.HttpStatus

class MailFailedException(
    message: String,
): CustomException(message, "Mail-002", HttpStatus.BAD_GATEWAY)
