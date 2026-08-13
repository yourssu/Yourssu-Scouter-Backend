package com.yourssu.scouter.hrms.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class IllegalMemberException(
    message: String,
) : CustomException(message, "Member-003", HttpStatus.BAD_REQUEST)
