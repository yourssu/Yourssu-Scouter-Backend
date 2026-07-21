package com.yourssu.scouter.common.support.implement.exception

import org.springframework.http.HttpStatus

class PartNotFoundException(
    message: String,
) : CustomException(message, "Part-001", HttpStatus.NOT_FOUND)
