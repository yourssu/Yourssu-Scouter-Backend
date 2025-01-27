package com.yourssu.scouter.common.implement.domain.semester

interface SemesterRepository {

    fun findById(semesterId: Long): Semester?
    fun findAll(): List<Semester>
}
