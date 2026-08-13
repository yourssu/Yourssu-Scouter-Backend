package com.yourssu.scouter.masterdata.support.exception

import com.yourssu.scouter.common.support.implement.exception.CustomException
import org.springframework.http.HttpStatus

class DepartmentNotFoundException(
    message: String,
) : CustomException(message, "Department-001", HttpStatus.NOT_FOUND)
