package com.yourssu.scouter.dummy

import com.yourssu.scouter.common.implement.support.initialization.CollegesAndDepartmentsInitializer
import com.yourssu.scouter.common.implement.support.initialization.DivisionsAndPartsInitializer
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
    private val applicantDummyForMemberSyncTest: ApplicantDummyForMemberSyncTest,
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        collegesAndDepartmentsInitializer.run()
        divisionsAndPartsInitializer.run()
        semesterDummy.run()
        memberDummy.run()
        applicantDummy.run()
        applicantDummyForMemberSyncTest.run()
    }
}
