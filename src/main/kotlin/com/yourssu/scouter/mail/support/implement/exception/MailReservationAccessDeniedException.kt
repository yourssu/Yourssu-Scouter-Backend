package com.yourssu.scouter.mail.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException

import org.springframework.http.HttpStatus

class MailReservationAccessDeniedException(
    message: String,
) : CustomException(message, "MailReservation-002", HttpStatus.FORBIDDEN)

