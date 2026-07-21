package com.yourssu.scouter.recruiting.applicant.storage

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantAnswer
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantAnswerRepository
import org.springframework.stereotype.Repository

@Repository
class ApplicantAnswerRepositoryImpl(
    private val jpaApplicantAnswerRepository: JpaApplicantAnswerRepository,
    private val jpaApplicantRepository: JpaApplicantRepository,
) : ApplicantAnswerRepository {

    override fun saveAll(answers: List<ApplicantAnswer>) {
        val entities = answers.map { answer ->
            ApplicantAnswerEntity(
                applicant = jpaApplicantRepository.getReferenceById(answer.applicantId),
                question = answer.question,
                answer = answer.answer,
                sectionId = answer.sectionId,
            )
        }

        jpaApplicantAnswerRepository.saveAll(entities)
    }

    override fun findAllByApplicantId(applicantId: Long): List<ApplicantAnswer> {
        return jpaApplicantAnswerRepository.findAllByApplicantId(applicantId).map { it.toDomain() }
    }
}
