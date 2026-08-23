package com.yourssu.scouter.member.core.implement

import com.yourssu.scouter.recruiting.support.business.EvaluatorDirectory
import com.yourssu.scouter.recruiting.support.business.EvaluatorInfo
import com.yourssu.scouter.recruiting.support.business.EvaluatorSummary
import org.springframework.stereotype.Component

import com.yourssu.scouter.member.support.converter.NicknameConverter

@Component
class MemberEvaluatorDirectory(
    private val memberReader: MemberReader,
) : EvaluatorDirectory {

    override fun findEvaluatorsByPartId(partId: Long): List<EvaluatorSummary> =
        memberReader.readAllActive()
            .filter { activeMember -> activeMember.member.parts.any { it.id == partId } }
            .map {
                EvaluatorSummary(
                    email = it.member.email,
                    name = it.member.name,
                    nickname = NicknameConverter.combine(it.member.nicknameEnglish, it.member.nicknameKorean),
                    memberId = it.member.id!!,
                )
            }

    override fun findEvaluatorInfo(email: String): EvaluatorInfo? {
        val member = memberReader.readAllActive().find { it.member.email == email }?.member ?: return null
        return EvaluatorInfo(
            memberId = member.id,
            nicknameEnglish = member.nicknameEnglish,
            nicknameKorean = member.nicknameKorean,
            partName = member.parts.firstOrNull()?.name,
        )
    }
}
