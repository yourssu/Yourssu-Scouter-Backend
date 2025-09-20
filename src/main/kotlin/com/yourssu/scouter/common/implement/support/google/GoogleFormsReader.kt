package com.yourssu.scouter.common.implement.support.google

import org.springframework.stereotype.Component

@Component
class GoogleFormsReader(
    private val googleFormsClient: GoogleFormsClient,
) {

    fun getUserResponses(authorizationHeader: String, formId: String): List<UserResponse> {
        val questionResponse = googleFormsClient.getFormQuestions(authorizationHeader, formId)
        val questionMap: Map<String, String?> = makeQuestionIdAndTitleMap(questionResponse)
        val groupQuestionMap: Map<String, String> = getGroupItems(questionResponse)
        val formResponses: GoogleFormResponses = googleFormsClient.getFormResponses(authorizationHeader, formId)

        return formResponses.responses.map { googleUserResponse ->
            UserResponse(
                responseId = googleUserResponse.responseId,
                createTime = googleUserResponse.createTime,
                respondentEmail = googleUserResponse.respondentEmail,
                lastSubmittedTime = googleUserResponse.lastSubmittedTime,
                responseItems = convertToResponseItems(googleUserResponse, questionMap, groupQuestionMap)
            )
        }
    }

    private fun makeQuestionIdAndTitleMap(questionResponse: GoogleFormQuestions) =
        questionResponse.items
            .mapNotNull { item ->
                val questionId = item.questionItem?.question?.questionId ?: return@mapNotNull null
                questionId to item.title
            }.toMap()

    private fun getGroupItems(questionResponse: GoogleFormQuestions) =
        questionResponse.items.flatMap { item ->
            if (item.questionGroupItem == null) return@flatMap emptyList()
            item.questionGroupItem.questions?.map { (questionId, rowQuestion) ->
                questionId to item.title + ":" + rowQuestion.title
            } as Iterable<Pair<String, String>>
        }.associate { it.first to it.second }

    private fun convertToResponseItems(
        googleUserResponse: GoogleUserResponse,
        questionMap: Map<String, String?>,
        groupQuestionMap: Map<String, String>
    ): List<ResponseItem> {
        val responseItems: List<ResponseItem> = googleUserResponse.answers.mapNotNull { (questionId, answer) ->
            val questionTitle = questionMap[questionId] ?: groupQuestionMap[questionId] ?: return@mapNotNull null
            val answerText = answer.textAnswers?.answers?.joinToString(", ") { it.value.toString() } ?: ""
            ResponseItem(questionTitle, answerText)
        }
        return responseItems
    }
}
