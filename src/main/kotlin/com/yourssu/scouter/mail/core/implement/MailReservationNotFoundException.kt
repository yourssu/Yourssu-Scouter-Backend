package com.yourssu.scouter.mail.core.implement

import com.yourssu.scouter.common.exception.CustomException

import org.springframework.http.HttpStatus

class MailReservationNotFoundException(
    message: String,
) : CustomException(message, "MailReservation-001", HttpStatus.NOT_FOUND)

