package com.yourssu.scouter.common.business.domain.semester

import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterReader
import org.springframework.stereotype.Service

@Service
class SemesterService(
    private val semesterReader: SemesterReader,
) {
    fun readAll(): List<SemesterDto> {
        val semesters: List<Semester> = semesterReader.readAll()

        return semesters.map { SemesterDto.from(it) }
    }
}
