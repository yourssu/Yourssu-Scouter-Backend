package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class MailFailedException(
    message: String,
): CustomException(message, "Mail-002", HttpStatus.BAD_GATEWAY)
