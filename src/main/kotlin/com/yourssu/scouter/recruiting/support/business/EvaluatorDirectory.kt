package com.yourssu.scouter.recruiting.support.business

interface EvaluatorDirectory {
    fun findEvaluatorsByPartId(partId: Long): List<EvaluatorSummary>
    fun findEvaluatorInfo(email: String): EvaluatorInfo?

    /** 여러 이메일의 평가자 정보를 한 번에 조회한다. 활동 멤버가 아닌 이메일은 결과에 포함되지 않는다. */
    fun findEvaluatorInfos(emails: Collection<String>): Map<String, EvaluatorInfo>
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
