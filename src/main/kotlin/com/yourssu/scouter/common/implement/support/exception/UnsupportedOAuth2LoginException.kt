package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class UnsupportedOAuth2LoginException(
    message: String,
) : CustomException(message, "Auth-002", HttpStatus.BAD_REQUEST)
