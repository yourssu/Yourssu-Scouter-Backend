package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class DocumentSectionNotFoundException(
    message: String,
) : CustomException(message, "DocumentSection-001", HttpStatus.NOT_FOUND)
