package com.yourssu.scouter.common.support.implement.exception

import org.springframework.http.HttpStatus

class SemesterNotFoundException(
    message: String,
) : CustomException(message, "Semester-001", HttpStatus.NOT_FOUND)
