package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class MailFileNotFoundException(
    message: String,
) : CustomException(message, "MailFile-001", HttpStatus.NOT_FOUND)
