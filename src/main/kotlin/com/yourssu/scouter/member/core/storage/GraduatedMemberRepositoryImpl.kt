package com.yourssu.scouter.member.core.storage

import com.yourssu.scouter.masterdata.part.implement.Part
import com.yourssu.scouter.member.core.implement.GraduatedMember
import com.yourssu.scouter.member.core.implement.GraduatedMemberRepository
import com.yourssu.scouter.member.core.implement.Member
import org.springframework.stereotype.Repository

@Repository
class GraduatedMemberRepositoryImpl(
    private val jpaGraduatedMemberRepository: JpaGraduatedMemberRepository,
    private val jpaMemberPartRepository: JpaMemberPartRepository,
) : GraduatedMemberRepository {

    override fun save(graduatedMember: GraduatedMember): GraduatedMember {
        val savedGraduatedMemberEntity: GraduatedMemberEntity =
            jpaGraduatedMemberRepository.save(GraduatedMemberEntity.from(graduatedMember))

        return savedGraduatedMemberEntity.toDomain(graduatedMember.member)
    }

    override fun findByMemberId(memberId: Long): GraduatedMember? {
        val graduatedMemberEntity = jpaGraduatedMemberRepository.findByMemberId(memberId)

        return graduatedMemberEntity?.let { fetchWithParts(it) }
    }

    override fun findAll(): List<GraduatedMember> {
        val graduatedMemberEntities = jpaGraduatedMemberRepository.findAll()

        return fetchAllWithParts(graduatedMemberEntities)
    }

    private fun fetchAllWithParts(entities: List<GraduatedMemberEntity>): List<GraduatedMember> {
        val partsByMemberId = jpaMemberPartRepository.findPartsGroupedByMemberId(entities.map { it.member.id!! })

        return entities.map { entity ->
            val member: Member = entity.member.toDomain(partsByMemberId[entity.member.id!!].orEmpty())
            entity.toDomain(member)
        }
    }

    private fun fetchWithParts(graduatedMemberEntity: GraduatedMemberEntity): GraduatedMember {
        val partEntities = jpaMemberPartRepository.findAllPartsByMemberId(graduatedMemberEntity.member.id!!)
        val parts: List<Part> = partEntities.map { it.toDomain() }
        val savedMember: Member = graduatedMemberEntity.member.toDomain(parts)

        return graduatedMemberEntity.toDomain(savedMember)
    }

    override fun findAllByName(name: String): List<GraduatedMember> {
        val graduatedMemberEntities = jpaGraduatedMemberRepository.findAllByName(name)

        return fetchAllWithParts(graduatedMemberEntities)
    }

    override fun findAllByNicknameKorean(nicknameKorean: String): List<GraduatedMember> {
        val graduatedMemberEntities = jpaGraduatedMemberRepository.findAllByNicknameKorean(nicknameKorean)

        return fetchAllWithParts(graduatedMemberEntities)
    }

    override fun findAllByNicknameEnglish(nicknameEnglish: String): List<GraduatedMember> {
        val graduatedMemberEntities = jpaGraduatedMemberRepository.findAllByNicknameEnglishIgnoreCase(nicknameEnglish)

        return fetchAllWithParts(graduatedMemberEntities)
    }

    override fun deleteByMemberId(memberId: Long) {
        jpaGraduatedMemberRepository.deleteByMemberId(memberId)
    }
}
