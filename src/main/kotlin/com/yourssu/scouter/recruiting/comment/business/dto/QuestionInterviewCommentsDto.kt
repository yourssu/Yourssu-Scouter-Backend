package com.yourssu.scouter.recruiting.comment.business.dto

data class QuestionInterviewCommentsDto(
    val sectionId: Long,
    val comments: List<DocumentCommentDto>,
)
