package com.yourssu.scouter.recruiting.interviewQuestion.business

import com.yourssu.scouter.masterdata.part.implement.PartReader
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
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class QuestionService(
    private val questionReader: QuestionReader,
    private val partReader: PartReader,
    private val questionWriter: QuestionWriter,
    private val requirementReader: InterviewRequirementReader,
) {

    @Transactional
    fun upsert(request: CreateQuestionsRequest): List<QuestionDto> {
        require(
            request.intro.mapNotNull { it.id }.size == request.intro.mapNotNull { it.id }.toSet().size,
        ) { "INTRO 질문 ID가 중복되었습니다." }
        require(
            request.outro.mapNotNull { it.id }.size == request.outro.mapNotNull { it.id }.toSet().size,
        ) { "OUTRO 질문 ID가 중복되었습니다." }
        require(
            request.culture.mapNotNull { it.id }.size == request.culture.mapNotNull { it.id }.toSet().size,
        ) { "CULTURE 질문 ID가 중복되었습니다." }

        validateCultureRequirements(request.culture.flatMap { it.requirementIds })

        val existing = questionReader.readAll()
            .filter {
                it.partId == null && it.category in setOf(
                    QuestionCategory.INTRO,
                    QuestionCategory.OUTRO,
                    QuestionCategory.CULTURE
                )
            }
        val byId = existing.associateBy { it.id }

        val items = request.intro.map { it to QuestionCategory.INTRO } +
                request.outro.map { it to QuestionCategory.OUTRO } +
                request.culture.map { it to QuestionCategory.CULTURE }

        items.forEach { (item, category) ->
            if (item.id != null) {
                require(byId[item.id]?.category == category) {
                    "카테고리가 일치하지 않거나 존재하지 않는 질문입니다: ${item.id}"
                }
            }
        }

        val retained = items.mapNotNull { it.first.id }.toSet()
        questionWriter.deleteAllByIdIn(existing.mapNotNull { it.id }.filterNot { it in retained })

        return items.map { (item, category) ->
            val question = Question(item.id, null, category, item.content, item.sortOrder, item.requirementIds)
            if (item.id == null) {
                QuestionDto.from(questionWriter.save(question))
            } else {
                questionWriter.update(question)
                QuestionDto.from(question)
            }
        }
    }

    private fun validateCultureRequirements(ids: List<Long>) {
        val requirements = requirementReader.readAllByIdIn(ids)
        require(
            requirements.size == ids.toSet().size &&
                    requirements.all { it.rubricType == RubricGroupType.CULTURE },
        ) { "CULTURE 질문에는 CULTURE 요구조건 ID만 사용할 수 있습니다." }
    }

    @Transactional
    fun upsertParts(partId: Long, request: UpdatePartQuestionsRequest): List<QuestionDto> {
        partReader.readById(partId)
        require(
            request.questions.mapNotNull { it.id }.size == request.questions.mapNotNull { it.id }.toSet().size,
        ) { "질문 ID가 중복되었습니다." }

        val existing = questionReader.readAll()
            .filter { it.partId == partId && it.category == QuestionCategory.PART }
        val existingById = existing.associateBy { it.id }

        request.questions.forEach { item ->
            validatePartRequirements(item.requirementIds)
            if (item.id != null) {
                require(existingById[item.id] != null) { "해당 파트에 존재하지 않는 질문입니다: ${item.id}" }
            }
        }

        val retainedIds = request.questions.mapNotNull { it.id }.toSet()
        questionWriter.deleteAllByIdIn(existing.mapNotNull { it.id }.filterNot { it in retainedIds })

        val allRequirementIds = request.questions.flatMap { it.requirementIds }.distinct()
        val requirementsById = requirementReader.readAllByIdIn(allRequirementIds).associateBy { it.id }

        return request.questions.map { item ->
            val question =
                Question(item.id, partId, QuestionCategory.PART, item.content, item.sortOrder, item.requirementIds)
            val savedQuestion = if (item.id == null) {
                questionWriter.save(question)
            } else {
                questionWriter.update(question)
                question
            }
            val requirementDtos = savedQuestion.requirementIds.mapNotNull { id ->
                requirementsById[id]?.let { QuestionRequirementDto(id, it.content) }
            }
            QuestionDto.from(savedQuestion, requirementDtos)
        }
    }

    private fun validatePartRequirements(ids: List<Long>) {
        require(ids.isNotEmpty()) { "파트 질문은 요구조건을 최소 1개 이상 매핑해야 합니다." }
        val requirements = requirementReader.readAllByIdIn(ids)
        require(
            requirements.size == ids.toSet().size &&
                    requirements.all {
                        it.rubricType in setOf(
                            RubricGroupType.TEAM,
                            RubricGroupType.JOB,
                            RubricGroupType.OTHER
                        )
                    },
        ) { "파트 질문에는 TEAM, JOB, OTHER 요구조건 ID만 사용할 수 있습니다." }
    }

    fun readAll(partId: Long): List<QuestionDto> {
        partReader.readById(partId)
        val questions = questionReader.readAll()
            .filter { it.partId == null || it.partId == partId }
        val requirements = requirementReader.readAllByIdIn(questions.flatMap { it.requirementIds }.distinct())
            .associateBy { it.id }
        return questions.map { question ->
            QuestionDto.from(
                question,
                question.requirementIds.mapNotNull { id ->
                    requirements[id]?.let { QuestionRequirementDto(id, it.content) }
                },
            )
        }
    }
}
