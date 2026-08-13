package com.yourssu.scouter.hrms.support.business.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class IllegalMemberUpdateException(
    message: String,
) : CustomException(message, "Member-002", HttpStatus.BAD_REQUEST)
