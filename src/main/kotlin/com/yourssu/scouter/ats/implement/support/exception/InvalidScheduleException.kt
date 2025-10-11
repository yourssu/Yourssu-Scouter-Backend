package com.yourssu.scouter.ats.implement.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class InvalidScheduleException(message: String) :
    CustomException(message, "Schedule-002", HttpStatus.BAD_REQUEST)