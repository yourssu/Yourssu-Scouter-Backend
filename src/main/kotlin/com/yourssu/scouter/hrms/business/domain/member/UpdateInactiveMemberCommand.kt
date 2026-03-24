package com.yourssu.scouter.hrms.business.domain.member

data class UpdateInactiveMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
    val inactiveMetadataPatch: UpdateInactiveMemberMetadataPatch? = null,
)
