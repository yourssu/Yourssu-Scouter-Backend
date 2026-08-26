package com.yourssu.scouter.member.core.business.dto

import com.yourssu.scouter.member.core.business.UpdateInactiveMemberMetadataPatch

data class UpdateInactiveMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
    val inactiveMetadataPatch: UpdateInactiveMemberMetadataPatch? = null,
)
