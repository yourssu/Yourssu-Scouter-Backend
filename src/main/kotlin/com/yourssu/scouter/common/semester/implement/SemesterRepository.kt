package com.yourssu.scouter.common.semester.implement

interface SemesterRepository {

    fun save(semester: Semester): Semester
    fun findById(semesterId: Long): Semester?
    fun find(semester: Semester): Semester?
    fun findAll(): List<Semester>
    fun deleteById(semesterId: Long)
}
