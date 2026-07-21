package com.yourssu.scouter.hrms.support.business.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class MemberNotRegisteredException(
    message: String,
) : CustomException(message, "Member-005", HttpStatus.UNAUTHORIZED)
