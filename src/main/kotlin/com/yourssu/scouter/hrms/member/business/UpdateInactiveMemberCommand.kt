package com.yourssu.scouter.hrms.member.business

data class UpdateInactiveMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
    val inactiveMetadataPatch: UpdateInactiveMemberMetadataPatch? = null,
)
