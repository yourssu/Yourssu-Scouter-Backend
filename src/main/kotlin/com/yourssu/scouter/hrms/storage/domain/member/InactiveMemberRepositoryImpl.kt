package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.InactiveMember
import com.yourssu.scouter.hrms.implement.domain.member.InactiveMemberRepository
import com.yourssu.scouter.hrms.implement.domain.member.Member
import org.springframework.stereotype.Repository

@Repository
class InactiveMemberRepositoryImpl(
    private val jpaInactiveMemberRepository: JpaInactiveMemberRepository,
    private val jpaMemberPartRepository: JpaMemberPartRepository,
) : InactiveMemberRepository {

    override fun findAll(): List<InactiveMember> {
        val inactiveMemberEntities = jpaInactiveMemberRepository.findAll()

        return inactiveMemberEntities.map { fetchWithParts(it) }
    }

    private fun fetchWithParts(inactiveMemberEntity: InactiveMemberEntity): InactiveMember {
        val partEntities = jpaMemberPartRepository.findAllPartsByMemberId(inactiveMemberEntity.member.id!!)
        val parts: List<Part> = partEntities.map { it.toDomain() }
        val savedMember: Member = inactiveMemberEntity.member.toDomain(parts)

        return inactiveMemberEntity.toDomain(savedMember)
    }

    override fun findAllByName(name: String): List<InactiveMember> {
        val inactiveMemberEntities = jpaInactiveMemberRepository.findAllByName(name)

        return inactiveMemberEntities.map { fetchWithParts(it) }
    }

    override fun findAllByNicknameKorean(nicknameKorean: String): List<InactiveMember> {
        val inactiveMemberEntities = jpaInactiveMemberRepository.findAllByNicknameKoreanIgnoreCase(nicknameKorean)

        return inactiveMemberEntities.map { fetchWithParts(it) }
    }

    override fun findAllByNicknameEnglish(nicknameEnglish: String): List<InactiveMember> {
        val inactiveMemberEntities = jpaInactiveMemberRepository.findAllByNicknameEnglishIgnoreCase(nicknameEnglish)

        return inactiveMemberEntities.map { fetchWithParts(it) }
    }
}
