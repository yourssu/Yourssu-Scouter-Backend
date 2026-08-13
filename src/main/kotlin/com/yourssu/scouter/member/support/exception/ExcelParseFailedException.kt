package com.yourssu.scouter.member.support.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class ExcelParseFailedException(
    message: String,
) : CustomException(message, "Member-004", HttpStatus.INTERNAL_SERVER_ERROR)
