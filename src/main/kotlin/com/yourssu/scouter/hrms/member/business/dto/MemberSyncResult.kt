package com.yourssu.scouter.hrms.member.business.dto

data class MemberSyncResult(
    val failureMessages: List<String> = emptyList(),
    val createdCount: Int = 0,
)
