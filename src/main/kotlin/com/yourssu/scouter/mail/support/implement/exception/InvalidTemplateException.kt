package com.yourssu.scouter.mail.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException

import org.springframework.http.HttpStatus

class InvalidTemplateException(
    message: String,
) : CustomException(message, "Template-Validation-Fail", HttpStatus.BAD_REQUEST)
