package com.yourssu.scouter.hrms.business.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class MemberNotRegisteredException(
    message: String,
) : CustomException(message, "Member-005", HttpStatus.UNAUTHORIZED)
