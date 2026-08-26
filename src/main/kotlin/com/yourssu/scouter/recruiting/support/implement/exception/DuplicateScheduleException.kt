package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class DuplicateScheduleException(
    message: String,
) : CustomException(message, "Schedule-001", HttpStatus.CONFLICT)