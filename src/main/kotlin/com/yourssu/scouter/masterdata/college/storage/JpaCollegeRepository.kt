package com.yourssu.scouter.masterdata.college.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaCollegeRepository : JpaRepository<CollegeEntity, Long> {
}
