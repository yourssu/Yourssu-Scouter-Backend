package com.yourssu.scouter.recruiting.interviewQuestion.implement

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationReader
import org.springframework.stereotype.Component

@Component
class PartLockPolicy(
    private final val applicantReader: ApplicantReader,
    private final val interviewEvaluationReader: InterviewEvaluationReader
) {


    fun isPartLocked(partId: Long, semesterId: Long): Boolean {
        val applicantIdsInPart = applicantReader.readByPartId(partId)
            .filter { it.applicationSemester.id == semesterId }
            .mapNotNull { it.id }
        if (applicantIdsInPart.isEmpty()) return false

        return interviewEvaluationReader.readAllByApplicantIdIn(applicantIdsInPart).isNotEmpty()
    }
}