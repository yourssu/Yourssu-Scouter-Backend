package com.yourssu.scouter.common.implement.support.google

import java.time.LocalDateTime

data class UserResponse(
    val responseId: String,
    val createTime: LocalDateTime,
    val respondentEmail: String,
    val lastSubmittedTime: LocalDateTime,
    val responseItems: List<ResponseItem>
) {

    constructor(
        responseId: String,
        createTime: String,
        respondentEmail: String,
        lastSubmittedTime: String,
        responseItems: List<ResponseItem>,
    ) : this(
        responseId = responseId,
        createTime = LocalDateTime.parse(createTime.substringBefore("Z")),
        respondentEmail = respondentEmail,
        lastSubmittedTime = LocalDateTime.parse(lastSubmittedTime.substringBefore("Z")),
        responseItems = responseItems
    )
}

data class ResponseItem(
    val question: String,
    val answer: String
)
