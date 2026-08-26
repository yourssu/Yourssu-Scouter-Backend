package com.yourssu.scouter.auth.support.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class DuplicateEmailException(
    message: String,
) : CustomException(message, "Auth-005", HttpStatus.CONFLICT)
