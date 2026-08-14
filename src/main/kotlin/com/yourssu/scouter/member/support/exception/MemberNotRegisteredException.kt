package com.yourssu.scouter.member.support.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class MemberNotRegisteredException(
    message: String,
) : CustomException(message, "Member-005", HttpStatus.UNAUTHORIZED)
