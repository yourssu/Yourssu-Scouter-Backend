package com.yourssu.scouter.recruiting.interviewQuestion.business

import com.yourssu.scouter.common.part.implement.PartReader
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.QuestionDto
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.QuestionRequirementDto
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionWriter
import com.yourssu.scouter.recruiting.interviewQuestion.implement.Question
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCategory
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirementReader
import com.yourssu.scouter.recruiting.interviewQuestion.application.dto.CreateQuestionsRequest
import com.yourssu.scouter.recruiting.interviewQuestion.application.dto.PartQuestionRequest
import com.yourssu.scouter.recruiting.interviewQuestion.application.dto.UpdatePartQuestionsRequest
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import org.springframework.stereotype.Service

@Service
class QuestionService(
    private val questionReader: QuestionReader,
    private val partReader: PartReader,
    private val questionWriter: QuestionWriter,
    private val requirementReader: InterviewRequirementReader,
) {

    fun create(request: CreateQuestionsRequest): List<QuestionDto> {
        val allCultureRequirementIds = request.culture.flatMap { it.requirementIds }
        val requirements = requirementReader.readAllByIdIn(allCultureRequirementIds)
        if (requirements.size != allCultureRequirementIds.toSet().size ||
            requirements.any { it.rubricType.name != "CULTURE" }) {
            throw IllegalArgumentException("CULTURE 질문에는 CULTURE 요구조건 ID만 사용할 수 있습니다.")
        }
        val items = request.global.map { it to QuestionCategory.GLOBAL } + request.culture.map { it to QuestionCategory.CULTURE }
        return items.map { (item, category) ->
            QuestionDto.from(questionWriter.save(Question(category = category, content = item.content, sortOrder = item.sortOrder, requirementIds = item.requirementIds)))
        }
    }

    fun upsertParts(partId: Long, request: UpdatePartQuestionsRequest): List<QuestionDto> {
        partReader.readById(partId)
        require(request.questions.mapNotNull { it.id }.size == request.questions.mapNotNull { it.id }.toSet().size) { "질문 ID가 중복되었습니다." }
        val existing = questionReader.readAll().filter { it.partId == partId && it.category == QuestionCategory.PART }
        val existingById = existing.associateBy { it.id }
        request.questions.forEach { item ->
            validatePartRequirements(item.requirementIds)
            if (item.id != null) {
                require(existingById[item.id] != null) { "해당 파트에 존재하지 않는 질문입니다: ${item.id}" }
            }
        }
        val retainedIds = request.questions.mapNotNull { it.id }.toSet()
        questionWriter.deleteAllByIdIn(existing.mapNotNull { it.id }.filterNot { it in retainedIds })
        return request.questions.map { item ->
            val question = Question(item.id, partId, QuestionCategory.PART, item.content, item.sortOrder, item.requirementIds)
            if (item.id == null) QuestionDto.from(questionWriter.save(question))
            else { questionWriter.update(question); QuestionDto.from(question) }
        }
    }

    private fun validatePartRequirements(ids: List<Long>) {
        require(ids.isNotEmpty()) { "파트 질문은 요구조건을 최소 1개 이상 매핑해야 합니다." }
        val requirements = requirementReader.readAllByIdIn(ids)
        require(requirements.size == ids.toSet().size && requirements.all { it.rubricType in setOf(RubricGroupType.TEAM, RubricGroupType.JOB, RubricGroupType.OTHER) }) {
            "파트 질문에는 TEAM, JOB, OTHER 요구조건 ID만 사용할 수 있습니다."
        }
    }

    fun readAll(partId: Long): List<QuestionDto> {
        partReader.readById(partId)
        val questions = questionReader.readAll()
            .filter { it.partId == null || it.partId == partId }
        val requirements = requirementReader.readAllByIdIn(questions.flatMap { it.requirementIds }.distinct())
            .associateBy { it.id }
        return questions.map { question ->
            QuestionDto.from(question, question.requirementIds.mapNotNull { id ->
                requirements[id]?.let { QuestionRequirementDto(id, it.content) }
            })
        }
    }
}
