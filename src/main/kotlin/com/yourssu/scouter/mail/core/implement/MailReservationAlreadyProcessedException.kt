package com.yourssu.scouter.mail.core.implement

import com.yourssu.scouter.common.exception.CustomException

import org.springframework.http.HttpStatus

class MailReservationAlreadyProcessedException(
    message: String,
) : CustomException(message, "MailReservation-003", HttpStatus.BAD_REQUEST)

