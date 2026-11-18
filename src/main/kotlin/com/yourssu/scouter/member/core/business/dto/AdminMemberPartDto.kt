package com.yourssu.scouter.member.core.business.dto

import com.yourssu.scouter.masterdata.part.business.dto.PartDto
import com.yourssu.scouter.member.core.implement.Member
import com.yourssu.scouter.member.core.implement.MemberRole
import com.yourssu.scouter.member.core.implement.MemberState
import com.yourssu.scouter.member.support.converter.NicknameConverter

/**
 * 파트 배정 화면 전용. 기존 MemberDto 는 createdTime!! 을 쓰기 때문에 감사 시각이 비어 있는 멤버에서 터진다.
 * 이 화면은 파트가 비어 있는 멤버까지 전부 보여야 해서 필요한 필드만 담는다.
 */
data class AdminMemberPartDto(
    val memberId: Long,
    val name: String,
    val email: String,
    val nickname: String,
    val role: MemberRole,
    val state: MemberState,
    val parts: List<PartDto>,
) {

    companion object {
        fun from(member: Member): AdminMemberPartDto = AdminMemberPartDto(
            memberId = member.id!!,
            name = member.name,
            email = member.email,
            nickname = NicknameConverter.combine(member.nicknameEnglish, member.nicknameKorean),
            role = member.role,
            state = member.state,
            parts = member.parts.map(PartDto::from),
        )
    }
}
