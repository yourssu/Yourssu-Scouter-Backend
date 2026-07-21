package com.yourssu.scouter.hrms.member.implement

import com.yourssu.scouter.recruiting.support.business.EvaluatorDirectory
import com.yourssu.scouter.recruiting.support.business.EvaluatorInfo
import com.yourssu.scouter.recruiting.support.business.EvaluatorSummary
import org.springframework.stereotype.Component

@Component
class MemberEvaluatorDirectory(
    private val memberReader: MemberReader,
) : EvaluatorDirectory {

    override fun findEvaluatorsByPartId(partId: Long): List<EvaluatorSummary> =
        memberReader.readAllActive()
            .filter { activeMember -> activeMember.member.parts.any { it.id == partId } }
            .map { EvaluatorSummary(email = it.member.email, name = it.member.name) }

    override fun findEvaluatorInfo(email: String): EvaluatorInfo? {
        val member = memberReader.readAllActive().find { it.member.email == email }?.member ?: return null
        return EvaluatorInfo(
            nicknameEnglish = member.nicknameEnglish,
            partName = member.parts.firstOrNull()?.name,
        )
    }
}
