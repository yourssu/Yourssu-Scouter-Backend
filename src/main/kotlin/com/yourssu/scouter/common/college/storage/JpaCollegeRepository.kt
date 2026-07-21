package com.yourssu.scouter.common.college.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaCollegeRepository : JpaRepository<CollegeEntity, Long> {
}
