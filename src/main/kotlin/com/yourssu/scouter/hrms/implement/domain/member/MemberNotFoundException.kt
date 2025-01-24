package com.yourssu.scouter.hrms.implement.domain.member

class MemberNotFoundException(
    override val message: String
) : RuntimeException(message)

