package com.yourssu.scouter.hrms.implement.support.exception

import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.springframework.http.HttpStatus

class ExcelParseFailedException(
    message: String,
) : CustomException(message, "Member-004", HttpStatus.INTERNAL_SERVER_ERROR)
