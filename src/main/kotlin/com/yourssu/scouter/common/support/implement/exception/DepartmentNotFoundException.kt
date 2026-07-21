package com.yourssu.scouter.common.support.implement.exception

import org.springframework.http.HttpStatus

class DepartmentNotFoundException(
    message: String,
) : CustomException(message, "Department-001", HttpStatus.NOT_FOUND)
