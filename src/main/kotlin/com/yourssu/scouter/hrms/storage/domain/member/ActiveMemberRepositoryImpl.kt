package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.ActiveMember
import com.yourssu.scouter.hrms.implement.domain.member.ActiveMemberRepository
import org.springframework.stereotype.Repository

@Repository
class ActiveMemberRepositoryImpl(
    private val jpaActiveMemberRepository: JpaActiveMemberRepository,
    private val jpaMemberPartRepository: JpaMemberPartRepository,
) : ActiveMemberRepository {

    override fun findAll(): List<ActiveMember> {
        val activeMemberEntities = jpaActiveMemberRepository.findAll()

        return fetchWithParts(activeMemberEntities)
    }

    private fun fetchWithParts(activeMemberEntities: List<ActiveMemberEntity>): List<ActiveMember> {
        return activeMemberEntities.map { activeMemberEntity ->
            val partEntities = jpaMemberPartRepository.findAllPartsByMemberId(activeMemberEntity.member.id!!)
            val parts: List<Part> = partEntities.map { it.toDomain() }

            activeMemberEntity.toDomain(parts)
        }
    }

    override fun findAllByName(name: String): List<ActiveMember> {
        val activeMemberEntities = jpaActiveMemberRepository.findAllByName(name)

        return fetchWithParts(activeMemberEntities)
    }

    override fun findAllByNicknameKorean(nicknameKorean: String): List<ActiveMember> {
        val activeMemberEntities = jpaActiveMemberRepository.findAllByNicknameKoreanIgnoreCase(nicknameKorean)

        return fetchWithParts(activeMemberEntities)
    }

    override fun findAllByNicknameEnglish(nicknameEnglish: String): List<ActiveMember> {
        val activeMemberEntities = jpaActiveMemberRepository.findAllByNicknameEnglishIgnoreCase(nicknameEnglish)

        return fetchWithParts(activeMemberEntities)
    }
}
