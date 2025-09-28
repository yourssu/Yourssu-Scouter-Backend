package com.yourssu.scouter.common.implement.support.google

import java.time.LocalDateTime

data class UserResponse(
    val responseId: String,
    val createTime: LocalDateTime,
    val respondentEmail: String?,
    val lastSubmittedTime: LocalDateTime?,
    val responseItems: List<ResponseItem> = emptyList(),
) {
    constructor(
        responseId: String,
        createTime: String?,
        respondentEmail: String?,
        lastSubmittedTime: String?,
        responseItems: List<ResponseItem>,
    ) : this(
        responseId = responseId,
        createTime = LocalDateTime.parse(createTime?.substringBefore("Z")),
        respondentEmail = respondentEmail,
        lastSubmittedTime = LocalDateTime.parse(lastSubmittedTime?.substringBefore("Z")),
        responseItems = responseItems
    )

    fun getAnswer(question: String?): String? {
        if (question == null) {
            return null
        }

        return responseItems.find { it.question.startsWith(question) }?.answer
    }

    fun getAll(question: String?): List<ResponseItem> {
        if (question == null) {
            return emptyList()
        }
        return responseItems.filter { it.question.startsWith(question) }
    }
}

data class ResponseItem(
    val question: String,
    val answer: String
)
