package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class MailReservationNotFoundException(
    message: String,
) : CustomException(message, "MailReservation-001", HttpStatus.NOT_FOUND)

