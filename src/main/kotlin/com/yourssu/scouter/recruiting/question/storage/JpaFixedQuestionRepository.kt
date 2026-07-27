package com.yourssu.scouter.recruiting.question.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaFixedQuestionRepository : JpaRepository<FixedQuestionEntity, Long> {

    fun findAllByIdIn(ids: Collection<Long>): List<FixedQuestionEntity>
}
