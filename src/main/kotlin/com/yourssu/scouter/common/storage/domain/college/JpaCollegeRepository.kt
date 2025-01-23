package com.yourssu.scouter.common.storage.domain.college

import org.springframework.data.jpa.repository.JpaRepository

interface JpaCollegeRepository : JpaRepository<CollegeEntity, Long> {
}
