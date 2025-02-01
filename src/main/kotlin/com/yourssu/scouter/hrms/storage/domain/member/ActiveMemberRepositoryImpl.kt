package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.ActiveMember
import com.yourssu.scouter.hrms.implement.domain.member.ActiveMemberRepository
import com.yourssu.scouter.hrms.implement.domain.member.Member
import org.springframework.stereotype.Repository

@Repository
class ActiveMemberRepositoryImpl(
    private val jpaActiveMemberRepository: JpaActiveMemberRepository,
    private val jpaMemberPartRepository: JpaMemberPartRepository,
) : ActiveMemberRepository {

    override fun save(activeMember: ActiveMember): ActiveMember {
        val savedActiveMemberEntity: ActiveMemberEntity =
            jpaActiveMemberRepository.save(ActiveMemberEntity.from(activeMember))

        return savedActiveMemberEntity.toDomain(activeMember.member)
    }

    override fun findAll(): List<ActiveMember> {
        val activeMemberEntities = jpaActiveMemberRepository.findAll()

        return activeMemberEntities.map { fetchWithParts(it) }
    }

    private fun fetchWithParts(activeMemberEntity: ActiveMemberEntity): ActiveMember {
        val partEntities = jpaMemberPartRepository.findAllPartsByMemberId(activeMemberEntity.member.id!!)
        val parts: List<Part> = partEntities.map { it.toDomain() }
        val savedMember: Member = activeMemberEntity.member.toDomain(parts)

        return activeMemberEntity.toDomain(savedMember)
    }

    override fun findAllByName(name: String): List<ActiveMember> {
        val activeMemberEntities = jpaActiveMemberRepository.findAllByName(name)

        return activeMemberEntities.map { fetchWithParts(it) }
    }

    override fun findAllByNicknameKorean(nicknameKorean: String): List<ActiveMember> {
        val activeMemberEntities = jpaActiveMemberRepository.findAllByNicknameKoreanIgnoreCase(nicknameKorean)

        return activeMemberEntities.map { fetchWithParts(it) }
    }

    override fun findAllByNicknameEnglish(nicknameEnglish: String): List<ActiveMember> {
        val activeMemberEntities = jpaActiveMemberRepository.findAllByNicknameEnglishIgnoreCase(nicknameEnglish)

        return activeMemberEntities.map { fetchWithParts(it) }
    }
}
