package com.yourssu.scouter.masterdata.support.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class SemesterNotFoundException(
    message: String,
) : CustomException(message, "Semester-001", HttpStatus.NOT_FOUND)
