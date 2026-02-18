package com.yourssu.scouter.common.implement.support.exception

import org.springframework.http.HttpStatus

class SemesterNotFoundException(
    message: String,
) : CustomException(message, "Semester-001", HttpStatus.NOT_FOUND)
