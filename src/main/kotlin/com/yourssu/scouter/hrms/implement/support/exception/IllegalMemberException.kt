package com.yourssu.scouter.hrms.implement.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class IllegalMemberException(
    message: String,
) : CustomException(message, "Member-003", HttpStatus.BAD_REQUEST)
