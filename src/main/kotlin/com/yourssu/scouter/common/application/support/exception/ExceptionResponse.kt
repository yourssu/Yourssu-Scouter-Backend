package com.yourssu.scouter.common.application.support.exception

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import org.springframework.http.HttpStatus

data class ExceptionResponse(

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    val timestamp: LocalDateTime,
    val status: Int,
    val errorCode: String,
    val message: String,
) {

    constructor(status: HttpStatus, errorCode: String, message: String) : this(
        timestamp = LocalDateTime.now(),
        status = status.value(),
        errorCode = errorCode,
        message = message,
    )
}
