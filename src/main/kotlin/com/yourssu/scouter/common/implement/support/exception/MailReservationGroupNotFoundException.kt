package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class MailReservationGroupNotFoundException(
    message: String,
) : CustomException(message, "MailReservationGroup-001", HttpStatus.NOT_FOUND)
