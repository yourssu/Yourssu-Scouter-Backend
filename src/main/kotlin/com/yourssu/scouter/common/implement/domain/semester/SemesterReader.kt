package com.yourssu.scouter.common.implement.domain.semester

import com.yourssu.scouter.common.implement.support.exception.SemesterNotFoundException
import java.time.Year
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class SemesterReader(
    private val semesterRepository: SemesterRepository,
) {

    fun readById(semesterId: Long): Semester {
        return semesterRepository.findById(semesterId) ?: throw SemesterNotFoundException("지정한 학기를 찾을 수 없습니다.")
    }

    fun read(semester: Semester): Semester {
        return semesterRepository.find(semester) ?: throw SemesterNotFoundException("지정한 학기를 찾을 수 없습니다.")
    }

    fun readAll(): List<Semester> = semesterRepository.findAll()

    fun readByString(applicationSemesterString: String): Semester {
        val yearString: String = applicationSemesterString.substringBefore("-")
        val yearValue = yearString.toInt() % 100 + 2000
        val termString: String = applicationSemesterString.substringAfter("-")

        val year: Year = Year.of(yearValue)
        val term: Term = Term.of(termString.toInt())
        val toFind = Semester(year = year, term = term)

        return semesterRepository.find(toFind) ?: throw SemesterNotFoundException("지정한 학기를 찾을 수 없습니다.")
    }
}
