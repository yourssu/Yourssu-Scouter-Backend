package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.recruiting.interview.implement.InterviewScript
import com.yourssu.scouter.recruiting.interview.implement.InterviewScriptRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class InterviewScriptRepositoryImpl(
    private val jpaInterviewScriptRepository: JpaInterviewScriptRepository,
) : InterviewScriptRepository {

    override fun findByPartId(partId: Long): InterviewScript? {
        return jpaInterviewScriptRepository.findByIdOrNull(partId)?.toDomain()
    }

    override fun upsert(script: InterviewScript): InterviewScript {
        val entity = InterviewScriptEntity(
            partId = script.partId,
            opening = script.opening,
            closing = script.closing,
        )

        return jpaInterviewScriptRepository.save(entity).toDomain()
    }
}
