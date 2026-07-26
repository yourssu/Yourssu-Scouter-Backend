package com.yourssu.scouter.recruiting.rubric.storage

import com.yourssu.scouter.common.part.storage.JpaPartRepository
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubric
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricMapper
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricWriter
import org.springframework.stereotype.Repository
@Repository
class InterviewRubricRepositoryImpl(
    private val jpaRepository: JpaInterviewRubricRepository,
    private val jpaPartRepository: JpaPartRepository,
    private val mapper: InterviewRubricMapper
) : InterviewRubricReader, InterviewRubricWriter {

    override fun getByPartIdAndSemester(partId: Long, semester: String): InterviewRubric {
        val entity = jpaRepository.findByPartIdAndSemester(partId, semester)
            ?: throw IllegalArgumentException("해당 파트의 면접 루브릭을 찾을 수 없습니다. partId=$partId")
        return mapper.toDomain(entity)
    }

    override fun findByPartIdAndSemester(partId: Long, semester: String): InterviewRubric? {
        return jpaRepository.findByPartIdAndSemester(partId, semester)?.let { mapper.toDomain(it) }
    }

    override fun save(interviewRubric: InterviewRubric): InterviewRubric {
        val entity = mapper.toEntity(
            domain = interviewRubric,
            partEntity = jpaPartRepository.getReferenceById(interviewRubric.partId)
        )
        val savedEntity = jpaRepository.save(entity)
        return mapper.toDomain(savedEntity)
    }
}
