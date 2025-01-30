package com.yourssu.scouter.common.business.domain.semester

import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterReader
import com.yourssu.scouter.common.implement.domain.semester.SemesterWriter
import org.springframework.stereotype.Service

@Service
class SemesterService(
    private val semesterWriter: SemesterWriter,
    private val semesterReader: SemesterReader,
) {

    fun create(year: Int, term: Int): Long {
        val toWriteSemester = Semester(year, term)
        val writtenSemester: Semester = semesterWriter.write(toWriteSemester)

        return writtenSemester.id!!
    }

    fun readAll(): List<SemesterDto> {
        val semesters: List<Semester> = semesterReader.readAll()

        return semesters.map { SemesterDto.from(it) }
    }

    fun deleteById(semesterId: Long) {
        val target: Semester = semesterReader.readById(semesterId)

        semesterWriter.delete(target)
    }
}
