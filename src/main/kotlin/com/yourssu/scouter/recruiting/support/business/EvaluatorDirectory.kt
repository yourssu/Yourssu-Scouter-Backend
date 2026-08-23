package com.yourssu.scouter.recruiting.support.business

interface EvaluatorDirectory {
    fun findEvaluatorsByPartId(partId: Long): List<EvaluatorSummary>
    fun findEvaluatorInfo(email: String): EvaluatorInfo?
}

data class EvaluatorSummary(
    val email: String,
    val name: String,
    val nickname: String,
    val memberId: Long,
)

data class EvaluatorInfo(
    val memberId: Long?,
    val nicknameEnglish: String?,
    val nicknameKorean: String?,
    val partName: String?,
)
