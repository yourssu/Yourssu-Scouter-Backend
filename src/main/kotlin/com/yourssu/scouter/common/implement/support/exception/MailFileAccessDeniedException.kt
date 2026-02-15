package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class MailFileAccessDeniedException(
    message: String,
) : CustomException(message, "MailFile-002", HttpStatus.FORBIDDEN)
