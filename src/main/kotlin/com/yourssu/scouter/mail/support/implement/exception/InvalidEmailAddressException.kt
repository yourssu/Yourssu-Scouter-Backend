package com.yourssu.scouter.mail.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException

import org.springframework.http.HttpStatus

class InvalidEmailAddressException(
    email: String,
) : CustomException(
    message = "유효하지 않은 이메일 주소입니다: $email",
    errorCode = "Mail-Invalid-Address",
    status = HttpStatus.BAD_REQUEST,
)
