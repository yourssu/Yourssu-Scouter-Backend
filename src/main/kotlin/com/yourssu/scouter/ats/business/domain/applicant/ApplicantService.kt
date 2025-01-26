package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantReader
import org.springframework.stereotype.Service

@Service
class ApplicantService(
    private val applicationReader: ApplicantReader,
) {

    fun readById(applicantId: Long): ApplicantDto {
        val applicant: Applicant = applicationReader.readById(applicantId)

        return ApplicantDto.from(applicant)
    }

    fun readAll(): List<ApplicantDto> {
        val applicants: List<Applicant> = applicationReader.readAll()

        return applicants.map { ApplicantDto.from(it) }
    }
}
