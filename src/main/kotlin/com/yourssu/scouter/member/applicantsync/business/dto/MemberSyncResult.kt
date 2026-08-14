package com.yourssu.scouter.member.applicantsync.business.dto

data class MemberSyncResult(
    val failureMessages: List<String> = emptyList(),
    val createdCount: Int = 0,
)
