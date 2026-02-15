package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class MailFileAlreadyUsedException(
    message: String,
) : CustomException(message, "MailFile-003", HttpStatus.CONFLICT)
