package com.yourssu.scouter.recruiting.question.storage

import com.yourssu.scouter.recruiting.question.implement.FixedQuestion
import com.yourssu.scouter.recruiting.question.implement.FixedQuestionRepository
import org.springframework.stereotype.Repository

@Repository
class FixedQuestionRepositoryImpl(
    private val jpaFixedQuestionRepository: JpaFixedQuestionRepository,
) : FixedQuestionRepository {

    override fun findAll(): List<FixedQuestion> {
        return jpaFixedQuestionRepository.findAll().map { it.toDomain() }
    }

    override fun findAllByIdIn(ids: Collection<Long>): List<FixedQuestion> {
        return jpaFixedQuestionRepository.findAllByIdIn(ids).map { it.toDomain() }
    }
}
