package com.yourssu.scouter.member.core.storage

import com.yourssu.scouter.masterdata.part.implement.Part
import com.yourssu.scouter.member.core.implement.InactiveMember
import com.yourssu.scouter.member.core.implement.InactiveMemberRepository
import com.yourssu.scouter.member.core.implement.Member
import org.springframework.stereotype.Repository

@Repository
class InactiveMemberRepositoryImpl(
    private val jpaInactiveMemberRepository: JpaInactiveMemberRepository,
    private val jpaMemberPartRepository: JpaMemberPartRepository,
) : InactiveMemberRepository {

    override fun save(inactiveMember: InactiveMember): InactiveMember {
        val savedInactiveMemberEntity: InactiveMemberEntity =
            jpaInactiveMemberRepository.save(InactiveMemberEntity.from(inactiveMember))

        return savedInactiveMemberEntity.toDomain(inactiveMember.member)
    }

    override fun findByMemberId(memberId: Long): InactiveMember? {
        val inactiveMemberEntity = jpaInactiveMemberRepository.findByMemberId(memberId)

        return inactiveMemberEntity?.let { fetchWithParts(it) }
    }

    override fun findAll(): List<InactiveMember> {
        val inactiveMemberEntities = jpaInactiveMemberRepository.findAll()

        return fetchAllWithParts(inactiveMemberEntities)
    }

    private fun fetchAllWithParts(entities: List<InactiveMemberEntity>): List<InactiveMember> {
        val partsByMemberId = jpaMemberPartRepository.findPartsGroupedByMemberId(entities.map { it.member.id!! })

        return entities.map { entity ->
            val member: Member = entity.member.toDomain(partsByMemberId[entity.member.id!!].orEmpty())
            entity.toDomain(member)
        }
    }

    private fun fetchWithParts(inactiveMemberEntity: InactiveMemberEntity): InactiveMember {
        val partEntities = jpaMemberPartRepository.findAllPartsByMemberId(inactiveMemberEntity.member.id!!)
        val parts: List<Part> = partEntities.map { it.toDomain() }
        val savedMember: Member = inactiveMemberEntity.member.toDomain(parts)

        return inactiveMemberEntity.toDomain(savedMember)
    }

    override fun findAllByName(name: String): List<InactiveMember> {
        val inactiveMemberEntities = jpaInactiveMemberRepository.findAllByName(name)

        return fetchAllWithParts(inactiveMemberEntities)
    }

    override fun findAllByNicknameKorean(nicknameKorean: String): List<InactiveMember> {
        val inactiveMemberEntities = jpaInactiveMemberRepository.findAllByNicknameKorean(nicknameKorean)

        return fetchAllWithParts(inactiveMemberEntities)
    }

    override fun findAllByNicknameEnglish(nicknameEnglish: String): List<InactiveMember> {
        val inactiveMemberEntities = jpaInactiveMemberRepository.findAllByNicknameEnglishIgnoreCase(nicknameEnglish)

        return fetchAllWithParts(inactiveMemberEntities)
    }

    override fun deleteByMemberId(memberId: Long) {
        jpaInactiveMemberRepository.deleteByMemberId(memberId)
    }
}
