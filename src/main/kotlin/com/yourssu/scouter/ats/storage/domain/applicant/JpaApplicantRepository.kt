package com.yourssu.scouter.ats.storage.domain.applicant

import org.springframework.data.jpa.repository.JpaRepository

interface JpaApplicantRepository : JpaRepository<ApplicantEntity, Long> {
}
