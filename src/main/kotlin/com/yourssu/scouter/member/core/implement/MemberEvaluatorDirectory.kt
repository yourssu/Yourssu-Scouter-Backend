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

    override fun findEvaluatorInfo(email: String): EvaluatorInfo? = findEvaluatorInfos(listOf(email))[email]

    override fun findEvaluatorInfos(emails: Collection<String>): Map<String, EvaluatorInfo> {
        if (emails.isEmpty()) {
            return emptyMap()
        }
        val targetEmails = emails.toSet()

        return memberReader.readAllActive()
            .map { it.member }
            .filter { it.email in targetEmails }
            .associate { member ->
                member.email to EvaluatorInfo(
                    memberId = member.id,
                    nicknameEnglish = member.nicknameEnglish,
                    nicknameKorean = member.nicknameKorean,
                    partName = member.parts.firstOrNull()?.name,
                )
            }
    }
}
