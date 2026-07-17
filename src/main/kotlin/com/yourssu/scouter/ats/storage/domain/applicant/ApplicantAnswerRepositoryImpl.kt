package com.yourssu.scouter.ats.storage.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantAnswer
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantAnswerRepository
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
