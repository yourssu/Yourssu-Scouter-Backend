package com.yourssu.scouter.auth.support.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class LastRoleHolderException(
    message: String,
) : CustomException(message, "Auth-007", HttpStatus.BAD_REQUEST)
