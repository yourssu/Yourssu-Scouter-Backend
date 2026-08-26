package com.yourssu.scouter.recruiting.applicant.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaApplicantSyncMappingRepository : JpaRepository<ApplicantSyncMappingEntity, Long> {

    fun findAllByApplicationSemesterId(applicationSemesterId: Long): List<ApplicantSyncMappingEntity>

    fun findByFormId(formId: String): ApplicantSyncMappingEntity?

    fun existsByApplicationSemester_IdAndPart_Id(applicationSemesterId: Long, partId: Long): Boolean
}
