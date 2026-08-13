package com.yourssu.scouter.hrms.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class MemberNotFoundException(
    message: String,
) : CustomException(message, "Member-001", HttpStatus.NOT_FOUND)
