package com.yourssu.scouter.common.semester.business

import com.yourssu.scouter.common.semester.business.dto.SemesterDto

import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.common.semester.implement.SemesterReader
import com.yourssu.scouter.common.semester.implement.SemesterWriter
import java.time.LocalDate
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

    fun readAllByReverseOrder(): List<SemesterDto> {
        val semesters: List<Semester> = semesterReader.readAll()

        return semesters.sortedDescending().map { SemesterDto.from(it) }
    }

    fun readByDate(date: LocalDate): SemesterDto {
        val semester: Semester = semesterReader.readByDate(date)

        return SemesterDto.from(semester)
    }

    fun deleteById(semesterId: Long) {
        val target: Semester = semesterReader.readById(semesterId)

        semesterWriter.delete(target)
    }
}
