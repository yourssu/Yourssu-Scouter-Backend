package com.yourssu.scouter.member.core.business

import com.yourssu.scouter.member.core.business.dto.MeResult

import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.member.core.business.dto.MemberDto
import com.yourssu.scouter.member.support.exception.MemberNotRegisteredException
import com.yourssu.scouter.member.core.implement.Member
import com.yourssu.scouter.member.core.implement.MemberReader
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
