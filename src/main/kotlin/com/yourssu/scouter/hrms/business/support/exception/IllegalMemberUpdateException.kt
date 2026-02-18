package com.yourssu.scouter.hrms.business.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class IllegalMemberUpdateException(
    message: String,
) : CustomException(message, "Member-002", HttpStatus.BAD_REQUEST)
