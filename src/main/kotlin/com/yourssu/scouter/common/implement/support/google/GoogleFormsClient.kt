package com.yourssu.scouter.common.implement.support.google

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(name = "googleFormsClient", url = "https://forms.googleapis.com/v1")
interface GoogleFormsClient {

    @GetMapping("/forms/{formId}")
    fun getFormQuestions(
        @RequestHeader("Authorization") authorizationHeader: String,
        @PathVariable("formId") formId: String
    ): GoogleFormQuestions

    @GetMapping("/forms/{formId}/responses")
    fun getFormResponses(
        @RequestHeader("Authorization") authorizationHeader: String,
        @PathVariable("formId") formId: String
    ): GoogleFormResponses
}

data class GoogleFormQuestions(
    val items: List<FormItem>?
)

data class FormItem(
    val itemId: String,
    val title: String,
    val questionItem: QuestionItem?
)

data class QuestionItem(
    val question: Question?
)

data class Question(
    val questionId: String
)

data class GoogleFormResponses(
    val responses: List<GoogleUserResponse>?
)

data class GoogleUserResponse(
    val responseId: String,
    val createTime: String,
    val answers: Map<String, Answer>
)

data class Answer(
    val textAnswers: TextAnswers?
)

data class TextAnswers(
    val answers: List<TextAnswer>
)

data class TextAnswer(
    val value: String
)
