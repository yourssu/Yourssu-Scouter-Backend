package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class PartNotFoundException(
    message: String,
) : CustomException(message, "Part-001", HttpStatus.NOT_FOUND)
