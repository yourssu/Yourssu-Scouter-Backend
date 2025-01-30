package com.yourssu.scouter.common.implement.support.initialization

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class DummyMaker(
    private val collegesAndDepartmentsInitializer: CollegesAndDepartmentsInitializer,
    private val divisionsAndPartsInitializer: DivisionsAndPartsInitializer,
    private val semesterDummy: SemesterDummy,
    private val memberDummy: MemberDummy,
    private val applicantDummy: ApplicantDummy,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        collegesAndDepartmentsInitializer.run()
        divisionsAndPartsInitializer.run()
        semesterDummy.run()
        memberDummy.run()
        applicantDummy.run()
    }
}
