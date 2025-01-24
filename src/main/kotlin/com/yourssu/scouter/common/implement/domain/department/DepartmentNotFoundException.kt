package com.yourssu.scouter.common.implement.domain.department

class DepartmentNotFoundException(
    override val message: String
) : RuntimeException(message)
