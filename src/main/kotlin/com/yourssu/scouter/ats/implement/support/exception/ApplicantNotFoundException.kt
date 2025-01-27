package com.yourssu.scouter.ats.implement.support.exception

class ApplicantNotFoundException(
    override val message: String
) : RuntimeException(message)
