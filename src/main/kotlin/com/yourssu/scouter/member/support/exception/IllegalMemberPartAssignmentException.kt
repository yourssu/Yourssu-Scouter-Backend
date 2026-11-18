package com.yourssu.scouter.member.support.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class IllegalMemberPartAssignmentException(
    message: String,
) : CustomException(message, "Member-008", HttpStatus.BAD_REQUEST)
