package com.yourssu.scouter.recruiting.interviewQuestion.implement

import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionCommand
import com.yourssu.scouter.recruiting.support.implement.exception.QuestionInvalidException
import org.springframework.stereotype.Component

@Component
class QuestionCatalogApplier (
    private final val questionReader: QuestionReader,
    private final val questionWriter: QuestionWriter,
    private final val assignedQuestionWriter: AssignedQuestionWriter
){

    fun createNewPartQuestions(questions : List<SaveAssignedQuestionCommand>,
                    applicantPartId: Long,
                    applicantSemesterId : Long,
                    partLocked : Boolean): Map<Int, Long>{
        if (partLocked) return emptyMap()

        val newPartQuestions =
            questions.withIndex().filter { (_, question) ->
                question.category == AssignedQuestionCategory.PART && question.sourceQuestionId == null
            }
        if (newPartQuestions.isEmpty()) return emptyMap()

        val existingPartQuestionCount =
            questionReader.readAllByPartIdAndSemesterId(applicantPartId, applicantSemesterId).size

        return newPartQuestions.mapIndexed { offset, (index, question) ->
            val content =
                question.content
                    ?: throw QuestionInvalidException("신규 PART 질문은 content가 필요합니다.")

            val saved =
                questionWriter.save(
                    Question(
                        partId = applicantPartId,
                        semesterId = applicantSemesterId,
                        category = QuestionCategory.PART,
                        content = content,
                        sortOrder = existingPartQuestionCount + offset,
                        requirementIds = question.requirementIds ?: emptyList(),
                    ),
                )
            index to saved.id!!
        }.toMap()

    }

    fun applyCatalogUpdatesAndDeletions(
        requestQuestions: List<SaveAssignedQuestionCommand>,
        resolvedQuestions: List<AssignedQuestion>,
        sourceQuestionsById: Map<Long?, Question>,
        applicantPartId: Long,
        applicantSemesterId: Long,
        partLocked: Boolean,
    ){
        updateCatalogQuestions(requestQuestions, sourceQuestionsById, partLocked)
        deleteRemovedPartQuestions(resolvedQuestions, applicantPartId, applicantSemesterId, partLocked)
    }

    private fun updateCatalogQuestions(
        questions: List<SaveAssignedQuestionCommand>,
        sourceQuestionsById: Map<Long?, Question>,
        partLocked: Boolean,
    ) {
        // 카탈로그 카테고리(INTRO/OUTRO/CULTURE/PART)의 content와 요구조건은 인스턴스가 아닌 원본 질문(Question)에 매핑된다.
        // INTRO/OUTRO/CULTURE는 이 요청으로 값을 변경할 수 없고, PART만 여기서 갱신 가능하다.
        // 단, 같은 파트의 다른 지원자가 이미 평가를 받아 파트가 잠긴 경우에는 PART 질문 변경 요청도 조용히 무시한다.
        questions
            .filter { it.sourceQuestionId != null }
            .forEach { question ->
                val sourceQuestion = sourceQuestionsById.getValue(question.sourceQuestionId)
                when (sourceQuestion.category) {
                    QuestionCategory.INTRO, QuestionCategory.OUTRO, QuestionCategory.CULTURE -> {
                        if (question.requirementIds != null) {
                            throw QuestionInvalidException(
                                "${sourceQuestion.category} 질문은 요구조건을 변경할 수 없습니다.",
                            )
                        }
                        if (question.content != null) {
                            throw QuestionInvalidException(
                                "${sourceQuestion.category} 질문은 content를 변경할 수 없습니다.",
                            )
                        }
                    }

                    QuestionCategory.PART -> {
                        if (partLocked) return@forEach

                        val updatedQuestion =
                            Question(
                                id = sourceQuestion.id,
                                partId = sourceQuestion.partId,
                                semesterId = sourceQuestion.semesterId,
                                category = sourceQuestion.category,
                                content = question.content ?: sourceQuestion.content,
                                sortOrder = sourceQuestion.sortOrder,
                                requirementIds = question.requirementIds ?: sourceQuestion.requirementIds,
                            )
                        questionWriter.update(updatedQuestion)
                    }
                }
            }
    }

    private fun deleteRemovedPartQuestions(
        questions: List<AssignedQuestion>,
        applicantPartId: Long,
        applicantSemesterId: Long,
        partLocked: Boolean,
    ) {
        if (partLocked) return

        val existingPartQuestionIds =
            questionReader.readAllByPartIdAndSemesterId(applicantPartId, applicantSemesterId).mapNotNull { it.id }.toSet()
        val submittedPartSourceIds =
            questions.filter { it.category == AssignedQuestionCategory.PART }.mapNotNull { it.sourceQuestionId }.toSet()

        val removedIds = existingPartQuestionIds - submittedPartSourceIds
        if (removedIds.isEmpty()) return

        assignedQuestionWriter.deleteAllBySourceQuestionIdIn(removedIds.toList())
        questionWriter.deleteAllByIdIn(removedIds)
    }
}