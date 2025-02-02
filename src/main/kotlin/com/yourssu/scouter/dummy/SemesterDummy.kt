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
        semesters.add(semesterRepository.save(Semester(null, Year.of(2016), Term.SPRING)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2017), Term.FALL)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2018), Term.SPRING)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2019), Term.FALL)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2020), Term.SPRING)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2020), Term.FALL)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2021), Term.SPRING)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2021), Term.FALL)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2022), Term.SPRING)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2022), Term.FALL)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2023), Term.SPRING)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2023), Term.FALL)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2024), Term.SPRING)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2024), Term.FALL)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2025), Term.SPRING)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2025), Term.FALL)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2026), Term.SPRING)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2026), Term.FALL)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2027), Term.SPRING)))
        semesters.add(semesterRepository.save(Semester(null, Year.of(2028), Term.FALL)))
    }
}
