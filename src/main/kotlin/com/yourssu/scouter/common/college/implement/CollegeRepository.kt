package com.yourssu.scouter.common.college.implement

interface CollegeRepository {

    fun save(college: College): College
    fun count(): Long
    fun findAll(): List<College>
}
