package com.yourssu.scouter.common.implement.support.google

import java.time.Instant

data class UserResponse(
    val responseId: String,
    val createTime: Instant,
    val respondentEmail: String?,
    val lastSubmittedTime: Instant?,
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
        createTime = Instant.parse(createTime),
        respondentEmail = respondentEmail,
        lastSubmittedTime = lastSubmittedTime?.let { Instant.parse(it) },
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
