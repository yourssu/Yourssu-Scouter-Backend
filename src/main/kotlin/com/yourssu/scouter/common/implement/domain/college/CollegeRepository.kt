package com.yourssu.scouter.common.implement.domain.college

import org.springframework.stereotype.Repository

@Repository
interface CollegeRepository {

    fun findAll(): List<College>
}
