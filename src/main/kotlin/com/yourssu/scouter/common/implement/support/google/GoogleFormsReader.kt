package com.yourssu.scouter.common.implement.support.google

import java.time.LocalDateTime
import org.springframework.stereotype.Component

@Component
class GoogleFormsReader(
    private val googleFormsClient: GoogleFormsClient,
) {

    fun getUserResponses(authorizationHeader: String, formId: String): List<UserResponse> {
        val questionResponse = googleFormsClient.getFormQuestions(authorizationHeader, formId)
        val questionMap: Map<String, String> = makeQuestionIdAndTitleMap(questionResponse)
        val formResponses: GoogleFormResponses = googleFormsClient.getFormResponses(authorizationHeader, formId)

        return formResponses.responses?.map { googleUserResponse ->
            UserResponse(
                responseId = googleUserResponse.responseId,
                createTime = LocalDateTime.parse(googleUserResponse.createTime.substringBefore("Z")),
                respondentEmail = googleUserResponse.respondentEmail,
                lastSubmittedTime = LocalDateTime.parse(googleUserResponse.lastSubmittedTime.substringBefore("Z")),
                responseItems = convertToResponseItems(googleUserResponse, questionMap)
            )
        } ?: emptyList()
    }

    private fun makeQuestionIdAndTitleMap(questionResponse: GoogleFormQuestions) =
        questionResponse.items
            ?.mapNotNull { item ->
                val questionId = item.questionItem?.question?.questionId ?: return@mapNotNull null
                questionId to item.title
            }?.toMap() ?: emptyMap()

    private fun convertToResponseItems(
        googleUserResponse: GoogleUserResponse,
        questionMap: Map<String, String>
    ): List<ResponseItem> {
        val responseItems: List<ResponseItem> = googleUserResponse.answers.mapNotNull { (questionId, answer) ->
            val questionTitle = questionMap[questionId] ?: return@mapNotNull null
            val answerText = answer.textAnswers?.answers?.joinToString(", ") { it.value } ?: ""
            ResponseItem(questionTitle, answerText)
        }
        return responseItems
    }
}

data class UserResponse(
    val responseId: String,
    val createTime: LocalDateTime,
    val respondentEmail: String,
    val lastSubmittedTime: LocalDateTime,
    val responseItems: List<ResponseItem>
)

data class ResponseItem(
    val question: String,
    val answer: String
)
