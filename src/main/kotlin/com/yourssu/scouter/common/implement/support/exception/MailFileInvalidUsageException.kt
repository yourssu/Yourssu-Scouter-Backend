package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class MailFileInvalidUsageException(
    message: String,
) : CustomException(message, "MailFile-004", HttpStatus.BAD_REQUEST)
