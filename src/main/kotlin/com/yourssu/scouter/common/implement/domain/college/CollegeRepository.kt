package com.yourssu.scouter.common.implement.domain.college

interface CollegeRepository {

    fun save(college: College): College
    fun count(): Long
    fun findAll(): List<College>
}
