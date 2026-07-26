package com.yourssu.scouter.recruiting.evaluation.business.dto

data class SaveInterviewEvaluationItemCommand(
    val evaluationItemId: Long,
    val score: Int,
)
