package com.yourssu.scouter.recruiting.interviewQuestion.business

import com.yourssu.scouter.masterdata.part.implement.PartReader
import com.yourssu.scouter.masterdata.semester.implement.SemesterReader
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
    private val semesterReader: SemesterReader,
    private val questionWriter: QuestionWriter,
    private val requirementReader: InterviewRequirementReader,
) {

    /**
     * INTRO / OUTRO / CULTURE 질문을 생성·수정합니다.
     * CULTURE 질문은 [semester] 학기에 귀속됩니다. (예: "2026-1")
     * INTRO / OUTRO 는 학기에 무관하게 공유되므로 semesterId=null 로 저장됩니다.
     */
    @Transactional
    fun upsert(semester: String, request: CreateQuestionsRequest): List<QuestionDto> {
        val semesterId = semesterReader.readByString(semester).id!!

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

        // INTRO / OUTRO : semester 무관 (semesterId = null)
        // CULTURE       : 해당 semester 에 귀속된 질문만 대상으로 upsert
        val existingIntroOutro = questionReader.readAll()
            .filter {
                it.partId == null && it.category in setOf(
                    QuestionCategory.INTRO,
                    QuestionCategory.OUTRO,
                )
            }
        val existingCulture = questionReader.readAllBySemesterId(semesterId)
            .filter { it.partId == null && it.category == QuestionCategory.CULTURE }

        val existing = existingIntroOutro + existingCulture
        val byId = existing.associateBy { it.id }

        val introItems = request.intro.map { it to QuestionCategory.INTRO }
        val outroItems = request.outro.map { it to QuestionCategory.OUTRO }
        val cultureItems = request.culture.map { it to QuestionCategory.CULTURE }
        val items = introItems + outroItems + cultureItems

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
            // CULTURE는 semesterId 할당, INTRO/OUTRO는 null
            val effectiveSemesterId = if (category == QuestionCategory.CULTURE) semesterId else null
            val question = Question(item.id, null, effectiveSemesterId, category, item.content, item.sortOrder, item.requirementIds)
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

    /**
     * 특정 파트의 공통 질문을 생성·수정합니다.
     * PART 질문은 [semester] 학기에 귀속됩니다. (예: "2026-1")
     */
    @Transactional
    fun upsertParts(partId: Long, semester: String, request: UpdatePartQuestionsRequest): List<QuestionDto> {
        partReader.readById(partId)
        val semesterId = semesterReader.readByString(semester).id!!
        require(
            request.questions.mapNotNull { it.id }.size == request.questions.mapNotNull { it.id }.toSet().size,
        ) { "질문 ID가 중복되었습니다." }

        val existing = questionReader.readAllByPartIdAndSemesterId(partId, semesterId)
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
                Question(item.id, partId, semesterId, QuestionCategory.PART, item.content, item.sortOrder, item.requirementIds)
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

    /**
     * 특정 파트 + 학기의 질문 목록을 조회합니다.
     * INTRO / OUTRO 는 학기에 무관하게 공유되므로 전체 목록에서 조회합니다.
     * CULTURE / PART 는 [semester] 학기에 귀속된 질문만 반환합니다. (예: "2026-1")
     */
    fun readAll(partId: Long, semester: String): List<QuestionDto> {
        partReader.readById(partId)
        val semesterId = semesterReader.readByString(semester).id!!

        val introOutroQuestions = questionReader.readAll()
            .filter { it.partId == null && it.category in setOf(QuestionCategory.INTRO, QuestionCategory.OUTRO) }
        val cultureQuestions = questionReader.readAllBySemesterId(semesterId)
            .filter { it.partId == null && it.category == QuestionCategory.CULTURE }
        val partQuestions = questionReader.readAllByPartIdAndSemesterId(partId, semesterId)

        val questions = introOutroQuestions + cultureQuestions + partQuestions

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

