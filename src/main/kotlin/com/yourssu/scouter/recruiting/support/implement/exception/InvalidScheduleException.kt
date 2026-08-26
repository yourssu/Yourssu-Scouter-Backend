package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class InvalidScheduleException(message: String) :
    CustomException(message, "Schedule-002", HttpStatus.BAD_REQUEST)