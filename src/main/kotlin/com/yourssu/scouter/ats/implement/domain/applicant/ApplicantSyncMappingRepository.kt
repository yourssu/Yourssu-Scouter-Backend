package com.yourssu.scouter.ats.implement.domain.applicant

interface ApplicantSyncMappingRepository {

    fun save(applicantSyncMapping: ApplicantSyncMapping)
    fun findAllByApplicationSemesterId(semesterId: Long): List<ApplicantSyncMapping>
    fun count(): Long
    fun existsByApplicationSemesterIdAndPartId(applicationSemesterId: Long, partId: Long): Boolean
}
