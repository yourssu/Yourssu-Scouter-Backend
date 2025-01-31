package com.yourssu.scouter.dummy

import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import com.yourssu.scouter.common.implement.domain.semester.Term
import java.time.Year
import org.springframework.stereotype.Component

@Component
class SemesterDummy(
    private val semesterRepository: SemesterRepository,
) {

    fun run() {
        val semesters = mutableListOf<Semester>()
        semesters.add(semesterRepository.save(Semester(null, Year.of(2023), Term.SPRING)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2023), Term.FALL)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2024), Term.SPRING)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2024), Term.FALL)))
    }
}
