package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class AssignedQuestionLockedException(
    message: String,
) : CustomException(message, "Question-007", HttpStatus.CONFLICT)
