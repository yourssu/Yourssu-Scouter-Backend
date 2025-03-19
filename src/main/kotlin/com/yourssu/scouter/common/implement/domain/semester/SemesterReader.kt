package com.yourssu.scouter.common.implement.domain.semester

import com.yourssu.scouter.common.implement.support.exception.SemesterNotFoundException
import java.time.LocalDate
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
        return semesterRepository.find(semester)
            ?: throw SemesterNotFoundException("${semester.year}-${semester.term.intValue}에 해당하는 학기 정보를 찾을 수 없습니다.")
    }

    fun readAll(): List<Semester> = semesterRepository.findAll()

    fun readByString(value: String): Semester {
        val yearString: String = value.substringBefore(Semester.DELIMITER)
        val termString: String = value.substringAfter(Semester.DELIMITER)
        val toFind: Semester = Semester.of(yearString, termString)

        return semesterRepository.find(toFind)
            ?: throw SemesterNotFoundException("${value}에 해당하는 학기 정보를 찾을 수 없습니다.")
    }

    fun readByDate(date: LocalDate): Semester {
        val toFind: Semester = Semester.of(date)

        return semesterRepository.find(toFind)
            ?: throw SemesterNotFoundException("${date}에 해당하는 학기 정보를 찾을 수 없습니다.")
    }
}
