package com.yourssu.scouter.mail.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException

import org.springframework.http.HttpStatus

class InvalidTemplateException(
    message: String,
) : CustomException(message, "Template-Validation-Fail", HttpStatus.BAD_REQUEST)
