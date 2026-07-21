package com.yourssu.scouter.hrms.support.implement.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class ExcelParseFailedException(
    message: String,
) : CustomException(message, "Member-004", HttpStatus.INTERNAL_SERVER_ERROR)
