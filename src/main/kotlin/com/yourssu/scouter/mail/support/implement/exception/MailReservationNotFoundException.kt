package com.yourssu.scouter.mail.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException

import org.springframework.http.HttpStatus

class MailReservationNotFoundException(
    message: String,
) : CustomException(message, "MailReservation-001", HttpStatus.NOT_FOUND)

