package com.yourssu.scouter.recruiting.interview.business.dto

import com.yourssu.scouter.recruiting.comment.business.dto.DocumentCommentDto

data class QuestionInterviewCommentsDto(
    val sectionId: Long,
    val comments: List<DocumentCommentDto>,
)
