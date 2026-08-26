package com.yourssu.scouter.recruiting.rubric.storage

import com.yourssu.scouter.masterdata.part.storage.JpaPartRepository
import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.masterdata.semester.storage.JpaSemesterRepository
import com.yourssu.scouter.masterdata.semester.storage.SemesterEntity
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubric
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricMapper
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricWriter
import com.yourssu.scouter.masterdata.support.exception.SemesterNotFoundException
import com.yourssu.scouter.recruiting.support.implement.exception.InterviewRubricNotFoundException
import org.springframework.stereotype.Repository

@Repository
class InterviewRubricRepositoryImpl(
    private val jpaRepository: JpaInterviewRubricRepository,
    private val jpaPartRepository: JpaPartRepository,
    private val jpaSemesterRepository: JpaSemesterRepository,
    private val mapper: InterviewRubricMapper
) : InterviewRubricReader, InterviewRubricWriter {

    override fun getByPartIdAndSemester(partId: Long, semester: Semester): InterviewRubric {
        val semesterEntity = jpaSemesterRepository.findByYearAndTerm(semester.year, semester.term)
            ?: throw SemesterNotFoundException("해당 학기 정보를 찾을 수 없습니다: ${semester.year}-${semester.term.intValue}")
        val entity = jpaRepository.findByPartIdAndSemester(partId, semesterEntity)
            ?: throw InterviewRubricNotFoundException("해당 파트의 면접 루브릭을 찾을 수 없습니다. partId=$partId")
        return mapper.toDomain(entity)
    }

    override fun findByPartIdAndSemester(partId: Long, semester: Semester): InterviewRubric? {
        val semesterEntity = jpaSemesterRepository.findByYearAndTerm(semester.year, semester.term) ?: return null
        return jpaRepository.findByPartIdAndSemester(partId, semesterEntity)?.let { mapper.toDomain(it) }
    }

    override fun save(interviewRubric: InterviewRubric): InterviewRubric {
        val semesterEntity = jpaSemesterRepository.findByYearAndTerm(interviewRubric.semester.year, interviewRubric.semester.term)
            ?: throw IllegalArgumentException("해당 학기 정보를 찾을 수 없습니다: ${interviewRubric.semester.year}-${interviewRubric.semester.term.intValue}")
        val entity = mapper.toEntity(
            domain = interviewRubric,
            partEntity = jpaPartRepository.getReferenceById(interviewRubric.partId)
        )
        entity.semester = semesterEntity
        val savedEntity = jpaRepository.save(entity)
        return mapper.toDomain(savedEntity)
    }

    override fun deleteByPartIdAndSemester(partId: Long, semester: Semester) {
        val semesterEntity = jpaSemesterRepository.findByYearAndTerm(semester.year, semester.term) ?: return
        jpaRepository.deleteByPartIdAndSemester(partId, semesterEntity)
    }
}
