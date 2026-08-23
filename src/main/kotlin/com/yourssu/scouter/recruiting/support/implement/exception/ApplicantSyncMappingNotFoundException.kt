package com.yourssu.scouter.recruiting.support.implement.exception

import com.yourssu.scouter.common.exception.CustomException
import org.springframework.http.HttpStatus

class ApplicantSyncMappingNotFoundException(
    message: String,
) : CustomException(message, "ApplicantSyncMapping-001", HttpStatus.NOT_FOUND)
