package com.yourssu.scouter.ats.storage.domain.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class DuplicateScheduleException(
    message: String,
) : CustomException(message, "Schedule-001", HttpStatus.CONFLICT)