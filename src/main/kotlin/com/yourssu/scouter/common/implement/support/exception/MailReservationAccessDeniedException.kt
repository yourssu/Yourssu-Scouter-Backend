package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class MailReservationAccessDeniedException(
    message: String,
) : CustomException(message, "MailReservation-002", HttpStatus.FORBIDDEN)

