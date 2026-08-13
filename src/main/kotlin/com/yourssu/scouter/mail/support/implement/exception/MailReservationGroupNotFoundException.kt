package com.yourssu.scouter.mail.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException

import org.springframework.http.HttpStatus

class MailReservationGroupNotFoundException(
    message: String,
) : CustomException(message, "MailReservationGroup-001", HttpStatus.NOT_FOUND)
