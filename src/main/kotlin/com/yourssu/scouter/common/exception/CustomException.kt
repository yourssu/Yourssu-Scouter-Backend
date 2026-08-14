package com.yourssu.scouter.common.exception

import org.springframework.http.HttpStatus

open class CustomException(
    override val message: String,
    val errorCode: String,
    val status: HttpStatus,
) : RuntimeException(message)
