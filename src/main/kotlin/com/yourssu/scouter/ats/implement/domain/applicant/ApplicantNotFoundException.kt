package com.yourssu.scouter.ats.implement.domain.applicant

class ApplicantNotFoundException(
    override val message: String
) : RuntimeException(message)
