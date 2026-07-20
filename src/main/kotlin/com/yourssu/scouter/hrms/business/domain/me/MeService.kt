package com.yourssu.scouter.hrms.business.domain.me

import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.hrms.business.domain.member.ActiveMemberDto
import com.yourssu.scouter.hrms.business.domain.member.CompletedMemberDto
import com.yourssu.scouter.hrms.business.domain.member.GraduatedMemberDto
import com.yourssu.scouter.hrms.business.domain.member.InactiveMemberDto
import com.yourssu.scouter.hrms.business.domain.member.MemberStateDto
import com.yourssu.scouter.hrms.business.domain.member.WithdrawnMemberDto
import com.yourssu.scouter.hrms.business.support.exception.MemberNotRegisteredException
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import org.springframework.stereotype.Service

@Service
class MeService(
    private val userReader: UserReader,
    private val memberReader: MemberReader,
) {

    fun getMe(userId: Long): MeResult {
        val user: User = userReader.readById(userId)
        val member: Member = memberReader.readByEmailOrNull(user.getEmailAddress())
            ?: throw MemberNotRegisteredException("등록된 멤버가 아닙니다.")
        val memberId: Long = member.id!!

        val memberState: MemberStateDto = when (member.state) {
            MemberState.ACTIVE -> ActiveMemberDto.from(memberReader.readActiveByMemberId(memberId))
            MemberState.INACTIVE -> InactiveMemberDto.from(memberReader.readInactiveByMemberId(memberId))
            MemberState.COMPLETED -> CompletedMemberDto.from(memberReader.readCompletedByMemberId(memberId))
            MemberState.GRADUATED -> GraduatedMemberDto.from(memberReader.readGraduatedByMemberId(memberId))
            MemberState.WITHDRAWN -> WithdrawnMemberDto.from(memberReader.readWithdrawnByMemberId(memberId))
        }

        return MeResult(
            profileImageUrl = user.userInfo.profileImageUrl,
            member = memberState,
        )
    }
}
