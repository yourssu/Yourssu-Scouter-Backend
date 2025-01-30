package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.InactiveMember
import com.yourssu.scouter.hrms.implement.domain.member.InactiveMemberRepository
import org.springframework.stereotype.Repository

@Repository
class InactiveMemberRepositoryImpl(
    private val jpaInactiveMemberRepository: JpaInactiveMemberRepository,
    private val jpaMemberPartRepository: JpaMemberPartRepository,
) : InactiveMemberRepository {

    override fun findAll(): List<InactiveMember> {
        val inactiveMemberEntities = jpaInactiveMemberRepository.findAll()

        return fetchWithParts(inactiveMemberEntities)
    }

    private fun fetchWithParts(inactiveMemberEntities: List<InactiveMemberEntity>): List<InactiveMember> {
        return inactiveMemberEntities.map { inactiveMemberEntity ->
            val partEntities = jpaMemberPartRepository.findAllPartsByMemberId(inactiveMemberEntity.member.id!!)
            val parts: List<Part> = partEntities.map { it.toDomain() }

            inactiveMemberEntity.toDomain(parts)
        }
    }

    override fun findAllByName(name: String): List<InactiveMember> {
        val inactiveMemberEntities = jpaInactiveMemberRepository.findAllByName(name)

        return fetchWithParts(inactiveMemberEntities)
    }

    override fun findAllByNicknameKorean(nicknameKorean: String): List<InactiveMember> {
        val inactiveMemberEntities = jpaInactiveMemberRepository.findAllByNicknameKoreanIgnoreCase(nicknameKorean)

        return fetchWithParts(inactiveMemberEntities)
    }

    override fun findAllByNicknameEnglish(nicknameEnglish: String): List<InactiveMember> {
        val inactiveMemberEntities = jpaInactiveMemberRepository.findAllByNicknameEnglishIgnoreCase(nicknameEnglish)

        return fetchWithParts(inactiveMemberEntities)
    }
}
