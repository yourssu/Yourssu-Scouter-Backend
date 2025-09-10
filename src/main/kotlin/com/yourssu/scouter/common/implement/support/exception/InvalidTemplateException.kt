package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class InvalidTemplateException(
    message: String,
) : CustomException(message, "Template-Validation-Fail", HttpStatus.BAD_REQUEST)
