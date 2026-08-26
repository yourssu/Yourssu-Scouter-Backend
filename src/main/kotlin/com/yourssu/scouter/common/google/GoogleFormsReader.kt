package com.yourssu.scouter.common.google

import org.springframework.stereotype.Component

@Component
class GoogleFormsReader(
    private val googleFormsClient: GoogleFormsClient,
) {

    fun getUserResponses(authorizationHeader: String, formId: String): List<UserResponse> {
        val questionResponse = googleFormsClient.getFormQuestions(authorizationHeader, formId)
        val questionMap: Map<String, String?> = makeQuestionIdAndTitleMap(questionResponse)
        val descriptiveQuestionIds: Set<String> = makeDescriptiveQuestionIds(questionResponse)
        val groupQuestionMap: Map<String, String> = getGroupItems(questionResponse)
        val questionOrder: List<String> = makeQuestionIdOrder(questionResponse)
        val formResponses: GoogleFormResponses = googleFormsClient.getFormResponses(authorizationHeader, formId)

        return formResponses.responses.map { googleUserResponse ->
            UserResponse(
                responseId = googleUserResponse.responseId,
                createTime = googleUserResponse.createTime,
                respondentEmail = googleUserResponse.respondentEmail,
                lastSubmittedTime = googleUserResponse.lastSubmittedTime,
                responseItems = convertToResponseItems(
                    googleUserResponse,
                    questionMap,
                    groupQuestionMap,
                    descriptiveQuestionIds,
                    questionOrder,
                )
            )
        }
    }

    private fun makeQuestionIdAndTitleMap(questionResponse: GoogleFormQuestions) =
        questionResponse.items
            .mapNotNull { item ->
                val questionId = item.questionItem?.question?.questionId ?: return@mapNotNull null
                questionId to item.title
            }.toMap()

    // 서술형(단락) 질문의 questionId 집합 — 이 질문들만 서류 평가 문항(DocumentSection) 생성 대상이 된다.
    private fun makeDescriptiveQuestionIds(questionResponse: GoogleFormQuestions): Set<String> =
        questionResponse.items
            .mapNotNull { item -> item.questionItem?.question }
            .filter { question -> question.textQuestion?.paragraph == true }
            .map { question -> question.questionId }
            .toSet()

    private fun getGroupItems(questionResponse: GoogleFormQuestions) =
        questionResponse.items.flatMap { item ->
            if (item.questionGroupItem == null) return@flatMap emptyList()
            item.questionGroupItem.questions?.map { (questionId, rowQuestion) ->
                questionId to item.title + ":" + rowQuestion.title
            } ?: emptyList()
        }.associate { it.first to it.second }

    // 응답은 Map으로 와서 순서가 보장되지 않으므로, 폼에 실제 배치된 문항 순서(items 순서)를 기준으로 정렬 기준을 만든다.
    private fun makeQuestionIdOrder(questionResponse: GoogleFormQuestions): List<String> =
        questionResponse.items.flatMap { item ->
            when {
                item.questionItem?.question != null -> listOf(item.questionItem.question.questionId)
                item.questionGroupItem?.questions != null -> item.questionGroupItem.questions.map { it.questionId }
                else -> emptyList()
            }
        }

    private fun convertToResponseItems(
        googleUserResponse: GoogleUserResponse,
        questionMap: Map<String, String?>,
        groupQuestionMap: Map<String, String>,
        descriptiveQuestionIds: Set<String>,
        questionOrder: List<String>,
    ): List<ResponseItem> {
        val orderIndex: Map<String, Int> = questionOrder.withIndex().associate { (index, questionId) -> questionId to index }

        val responseItems: List<ResponseItem> = googleUserResponse.answers.mapNotNull { (questionId, answer) ->
            val questionTitle = questionMap[questionId] ?: groupQuestionMap[questionId] ?: return@mapNotNull null
            val answerText = answer.textAnswers?.answers?.joinToString(", ") { it.value.toString() } ?: ""
            questionId to ResponseItem(questionTitle, answerText, isDescriptive = questionId in descriptiveQuestionIds)
        }.sortedBy { (questionId, _) -> orderIndex[questionId] ?: Int.MAX_VALUE }
            .map { (_, responseItem) -> responseItem }
        return responseItems
    }
}
