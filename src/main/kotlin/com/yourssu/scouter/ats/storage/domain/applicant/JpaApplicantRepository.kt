package com.yourssu.scouter.ats.storage.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import org.springframework.data.jpa.repository.JpaRepository

interface JpaApplicantRepository : JpaRepository<ApplicantEntity, Long> {

    fun findAllByName(name: String): List<ApplicantEntity>
    fun findAllByState(state: ApplicantState): List<ApplicantEntity>
    fun findAllByApplicationSemesterId(semesterId: Long): List<ApplicantEntity>
}
