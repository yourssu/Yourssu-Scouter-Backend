package com.yourssu.scouter.hrms.business.domain.member

data class MemberSyncResult(
    val failureMessages: List<String> = emptyList(),
    val createdCount: Int = 0,
)
