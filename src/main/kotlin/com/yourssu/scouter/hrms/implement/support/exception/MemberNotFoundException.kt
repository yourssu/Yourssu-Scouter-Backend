package com.yourssu.scouter.hrms.implement.support.exception

class MemberNotFoundException(
    override val message: String
) : RuntimeException(message)

