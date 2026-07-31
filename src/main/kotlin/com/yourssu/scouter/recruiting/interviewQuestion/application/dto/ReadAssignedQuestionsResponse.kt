package com.yourssu.scouter.recruiting.interviewQuestion.application.dto

import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.AssignedQuestionsDto

data class ReadAssignedQuestionsResponse(
    val questions: List<ReadAssignedQuestionResponse>,
) {
    companion object {
        fun from(dto: AssignedQuestionsDto): ReadAssignedQuestionsResponse = ReadAssignedQuestionsResponse(
            questions = dto.questions.map(ReadAssignedQuestionResponse::from),
        )
    }
}
