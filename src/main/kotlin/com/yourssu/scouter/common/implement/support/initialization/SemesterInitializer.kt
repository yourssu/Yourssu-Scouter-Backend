package com.yourssu.scouter.common.implement.support.initialization

import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.SemesterRepository
import com.yourssu.scouter.common.implement.domain.semester.Term
import java.time.Year
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Order(3)
@Transactional
class SemesterInitializer(
    private val semesterRepository: SemesterRepository,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (alreadyInitialized()) {
            return
        }

        semesterRepository.save(Semester(year = Year.of(2099), term = Term.SPRING))
        semesterRepository.save(Semester(year = Year.of(2099), term = Term.FALL))
        semesterRepository.save(Semester(year = Year.of(2018), term = Term.SPRING))
        semesterRepository.save(Semester(year = Year.of(2018), term = Term.FALL))
        semesterRepository.save(Semester(year = Year.of(2019), term = Term.SPRING))
        semesterRepository.save(Semester(year = Year.of(2019), term = Term.FALL))
        semesterRepository.save(Semester(year = Year.of(2020), term = Term.SPRING))
        semesterRepository.save(Semester(year = Year.of(2020), term = Term.FALL))
        semesterRepository.save(Semester(year = Year.of(2021), term = Term.SPRING))
        semesterRepository.save(Semester(year = Year.of(2021), term = Term.FALL))
        semesterRepository.save(Semester(year = Year.of(2022), term = Term.SPRING))
        semesterRepository.save(Semester(year = Year.of(2022), term = Term.FALL))
        semesterRepository.save(Semester(year = Year.of(2023), term = Term.SPRING))
        semesterRepository.save(Semester(year = Year.of(2023), term = Term.FALL))
        semesterRepository.save(Semester(year = Year.of(2024), term = Term.SPRING))
        semesterRepository.save(Semester(year = Year.of(2024), term = Term.FALL))
        semesterRepository.save(Semester(year = Year.of(2025), term = Term.SPRING))
        semesterRepository.save(Semester(year = Year.of(2025), term = Term.FALL))
        semesterRepository.save(Semester(year = Year.of(2026), term = Term.SPRING))
        semesterRepository.save(Semester(year = Year.of(2026), term = Term.FALL))
        semesterRepository.save(Semester(year = Year.of(2027), term = Term.SPRING))
        semesterRepository.save(Semester(year = Year.of(2027), term = Term.FALL))
        semesterRepository.save(Semester(year = Year.of(2028), term = Term.SPRING))
        semesterRepository.save(Semester(year = Year.of(2028), term = Term.FALL))
    }

    private fun alreadyInitialized() = semesterRepository.findAll().isNotEmpty()
}
