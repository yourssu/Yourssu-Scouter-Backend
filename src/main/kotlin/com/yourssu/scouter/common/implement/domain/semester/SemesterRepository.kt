package com.yourssu.scouter.common.implement.domain.semester

interface SemesterRepository {

    fun save(semester: Semester): Semester
    fun findById(semesterId: Long): Semester?
    fun findAll(): List<Semester>
    fun deleteById(semesterId: Long)
}
