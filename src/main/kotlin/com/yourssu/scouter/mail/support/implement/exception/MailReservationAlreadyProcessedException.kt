package com.yourssu.scouter.mail.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException

import org.springframework.http.HttpStatus

class MailReservationAlreadyProcessedException(
    message: String,
) : CustomException(message, "MailReservation-003", HttpStatus.BAD_REQUEST)

