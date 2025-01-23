package com.yourssu.scouter.common.implement.support.initialization

import com.yourssu.scouter.common.implement.domain.college.CollegeRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class DataInitializer(
    private val collegeRepository: CollegeRepository,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        initializeColleges()
    }

    private fun initializeColleges() {
        if (collegeRepository.count() == 0L) {
            collegeRepository.saveAll(InitialColleges.getData())
        }
    }
}
