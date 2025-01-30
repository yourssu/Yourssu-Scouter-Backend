package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.GraduatedMember
import com.yourssu.scouter.hrms.implement.domain.member.GraduatedMemberRepository
import org.springframework.stereotype.Repository

@Repository
class GraduatedMemberRepositoryImpl(
    private val jpaGraduatedMemberRepository: JpaGraduatedMemberRepository,
    private val jpaMemberPartRepository: JpaMemberPartRepository,
) : GraduatedMemberRepository {

    override fun findAll(): List<GraduatedMember> {
        val graduatedMemberEntities = jpaGraduatedMemberRepository.findAll()

        return fetchWithParts(graduatedMemberEntities)
    }

    private fun fetchWithParts(graduatedMemberEntities: List<GraduatedMemberEntity>): List<GraduatedMember> {
        return graduatedMemberEntities.map { graduatedMemberEntity ->
            val partEntities = jpaMemberPartRepository.findAllPartsByMemberId(graduatedMemberEntity.member.id!!)
            val parts: List<Part> = partEntities.map { it.toDomain() }

            graduatedMemberEntity.toDomain(parts)
        }
    }

    override fun findAllByName(name: String): List<GraduatedMember> {
        val graduatedMemberEntities = jpaGraduatedMemberRepository.findAllByName(name)

        return fetchWithParts(graduatedMemberEntities)
    }

    override fun findAllByNicknameKorean(nicknameKorean: String): List<GraduatedMember> {
        val graduatedMemberEntities = jpaGraduatedMemberRepository.findAllByNicknameKoreanIgnoreCase(nicknameKorean)

        return fetchWithParts(graduatedMemberEntities)
    }

    override fun findAllByNicknameEnglish(nicknameEnglish: String): List<GraduatedMember> {
        val graduatedMemberEntities = jpaGraduatedMemberRepository.findAllByNicknameEnglishIgnoreCase(nicknameEnglish)

        return fetchWithParts(graduatedMemberEntities)
    }
}
