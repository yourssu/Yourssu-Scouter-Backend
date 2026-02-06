package com.yourssu.scouter.hrms.business.domain.me

import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.hrms.business.domain.member.MemberDto
import com.yourssu.scouter.hrms.business.support.exception.MemberNotRegisteredException
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
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

        return MeResult(
            profileImageUrl = user.userInfo.profileImageUrl,
            member = MemberDto.from(member),
        )
    }
}
