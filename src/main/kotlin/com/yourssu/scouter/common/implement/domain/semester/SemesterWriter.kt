package com.yourssu.scouter.common.implement.domain.semester

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class SemesterWriter(
    private val semesterRepository: SemesterRepository,
) {

    fun write(semester: Semester): Semester {
        return semesterRepository.save(semester)
    }

    fun delete(semester: Semester) {
        semesterRepository.deleteById(semester.id!!)
    }
}
