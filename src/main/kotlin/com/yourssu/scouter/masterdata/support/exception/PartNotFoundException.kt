package com.yourssu.scouter.masterdata.support.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class PartNotFoundException(
    message: String,
) : CustomException(message, "Part-001", HttpStatus.NOT_FOUND)
