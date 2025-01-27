package com.yourssu.scouter.common.implement.support.exception

class DepartmentNotFoundException(
    override val message: String
) : RuntimeException(message)
