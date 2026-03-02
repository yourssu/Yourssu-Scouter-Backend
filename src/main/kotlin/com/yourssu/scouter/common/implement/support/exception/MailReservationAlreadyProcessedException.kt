package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class MailReservationAlreadyProcessedException(
    message: String,
) : CustomException(message, "MailReservation-003", HttpStatus.BAD_REQUEST)

