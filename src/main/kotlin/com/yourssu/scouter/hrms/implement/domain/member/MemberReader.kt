package com.yourssu.scouter.hrms.implement.domain.member

import com.yourssu.scouter.hrms.implement.support.exception.MemberNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class MemberReader(
    private val activeMemberRepository: ActiveMemberRepository,
    private val inactiveMemberRepository: InactiveMemberRepository,
    private val graduatedMemberRepository: GraduatedMemberRepository,
    private val withdrawnMemberRepository: WithdrawnMemberRepository,
) {

    fun readAllActive(): List<ActiveMember> {
        return activeMemberRepository.findAll()
    }

    fun readAllInactive(): List<InactiveMember> {
        return inactiveMemberRepository.findAll()
    }

    fun readAllGraduated(): List<GraduatedMember> {
        return graduatedMemberRepository.findAll()
    }

    fun readAllWithdrawn(): List<WithdrawnMember> {
        return withdrawnMemberRepository.findAll()
    }

    fun searchAllActiveByNameOrNickname(name: String): List<ActiveMember> {
        val members = listOf(
            activeMemberRepository.findAllByName(name),
            activeMemberRepository.findAllByNicknameEnglish(name),
            activeMemberRepository.findAllByNicknameKorean(name),
        ).flatten()

        return members.distinct()
    }

    fun searchAllInactiveByNameOrNickname(query: String): List<InactiveMember> {
        val members = listOf(
            inactiveMemberRepository.findAllByName(query),
            inactiveMemberRepository.findAllByNicknameEnglish(query),
            inactiveMemberRepository.findAllByNicknameKorean(query),
        ).flatten()

        return members.distinct()
    }

    fun searchAllGraduatedByNameOrNickname(query: String): List<GraduatedMember> {
        val members = listOf(
            graduatedMemberRepository.findAllByName(query),
            graduatedMemberRepository.findAllByNicknameEnglish(query),
            graduatedMemberRepository.findAllByNicknameKorean(query),
        ).flatten()

        return members.distinct()
    }

    fun searchAllWithdrawnByNameOrNickname(query: String): List<WithdrawnMember> {
        val members = listOf(
            withdrawnMemberRepository.findAllByName(query),
            withdrawnMemberRepository.findAllByNicknameEnglish(query),
            withdrawnMemberRepository.findAllByNicknameKorean(query),
        ).flatten()

        return members.distinct()
    }

    fun readActiveByMemberId(memberId: Long): ActiveMember {
        return activeMemberRepository.findByMemberId(memberId)
            ?: throw MemberNotFoundException("해당하는 회원을 찾을 수 없습니다.")
    }
}
