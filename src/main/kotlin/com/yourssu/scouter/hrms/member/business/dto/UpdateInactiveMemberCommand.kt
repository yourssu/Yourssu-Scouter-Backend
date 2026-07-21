package com.yourssu.scouter.hrms.member.business.dto

import com.yourssu.scouter.hrms.member.business.UpdateInactiveMemberMetadataPatch

data class UpdateInactiveMemberCommand(
    val targetMemberId: Long,
    val updateMemberInfoCommand: UpdateMemberInfoCommand? = null,
    val inactiveMetadataPatch: UpdateInactiveMemberMetadataPatch? = null,
)
