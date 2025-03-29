package com.yourssu.scouter.ats.implement.domain.applicant

interface ApplicantSyncMappingRepository {

    fun save(applicantSyncMapping: ApplicantSyncMapping)
    fun findAllByApplicantSemesterId(semesterId: Long): List<ApplicantSyncMapping>
    fun count(): Long
}
