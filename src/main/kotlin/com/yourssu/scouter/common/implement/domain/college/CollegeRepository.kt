package com.yourssu.scouter.common.implement.domain.college

import org.springframework.stereotype.Repository

@Repository
interface CollegeRepository {

    fun saveAll(colleges: List<College>)
    fun count(): Long
    fun findAll(): List<College>
}
