package com.yourssu.scouter.common.implement.domain.college

import org.springframework.stereotype.Repository

@Repository
interface CollegeRepository {

    fun save(college: College): College
    fun count(): Long
    fun findAll(): List<College>
}
