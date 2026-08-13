package com.yourssu.scouter.hrms.support.business.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class MemberAccessDeniedException(
    message: String,
) : CustomException(message, "Member-006", HttpStatus.FORBIDDEN)

