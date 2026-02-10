package com.yourssu.scouter.common.application.support.exception

import java.time.Instant
import org.springframework.http.HttpStatus

data class ExceptionResponse(

    val timestamp: Instant,
    val status: Int,
    val errorCode: String,
    val message: String,
) {

    constructor(status: HttpStatus, errorCode: String, message: String) : this(
        timestamp = Instant.now(),
        status = status.value(),
        errorCode = errorCode,
        message = message,
    )
}
