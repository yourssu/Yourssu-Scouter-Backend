package com.yourssu.scouter.hrms.implement.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class MemberNotFoundException(
    message: String,
) : CustomException(message, "Member-001", HttpStatus.NOT_FOUND)
