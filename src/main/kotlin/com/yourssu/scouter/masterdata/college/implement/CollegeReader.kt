package com.yourssu.scouter.masterdata.college.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class CollegeReader(
    private val collegeRepository: CollegeRepository,
) {

    fun readAll(): List<College> {
        return collegeRepository.findAll()
    }
}
