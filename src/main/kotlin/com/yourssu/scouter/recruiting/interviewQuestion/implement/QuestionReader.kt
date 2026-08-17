package com.yourssu.scouter.recruiting.interviewQuestion.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class QuestionReader(
    private val questionRepository: QuestionRepository,
) {

    fun readAll(): List<Question> {
        return questionRepository.findAll().sortedWith(compareBy({ it.category }, { it.partId ?: 0L }, { it.sortOrder }))
    }

    fun readAllByIdIn(ids: Collection<Long>): List<Question> {
        return questionRepository.findAllByIdIn(ids)
    }

    /** CULTURE/PART 질문을 학기 ID 기준으로 조회합니다. INTRO/OUTRO(semesterId=null)는 포함되지 않습니다. */
    fun readAllBySemesterId(semesterId: Long): List<Question> {
        return questionRepository.findAllBySemesterId(semesterId)
            .sortedWith(compareBy({ it.category }, { it.partId ?: 0L }, { it.sortOrder }))
    }

    /** 특정 파트 + 학기의 PART 질문만 조회합니다. */
    fun readAllByPartIdAndSemesterId(partId: Long, semesterId: Long): List<Question> {
        return questionRepository.findAllByPartIdAndSemesterId(partId, semesterId)
            .sortedBy { it.sortOrder }
    }
}

