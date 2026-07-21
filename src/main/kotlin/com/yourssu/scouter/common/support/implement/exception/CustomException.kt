package com.yourssu.scouter.common.support.implement.exception

import org.springframework.http.HttpStatus

open class CustomException(
    override val message: String,
    val errorCode: String,
    val status: HttpStatus,
) : RuntimeException(message)
