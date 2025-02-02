package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.GraduatedMember
import com.yourssu.scouter.hrms.implement.domain.member.GraduatedMemberRepository
import com.yourssu.scouter.hrms.implement.domain.member.Member
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

        return graduatedMemberEntities.map { fetchWithParts(it) }
    }

    private fun fetchWithParts(graduatedMemberEntity: GraduatedMemberEntity): GraduatedMember {
        val partEntities = jpaMemberPartRepository.findAllPartsByMemberId(graduatedMemberEntity.member.id!!)
        val parts: List<Part> = partEntities.map { it.toDomain() }
        val savedMember: Member = graduatedMemberEntity.member.toDomain(parts)

        return graduatedMemberEntity.toDomain(savedMember)
    }


    override fun findAllByName(name: String): List<GraduatedMember> {
        val graduatedMemberEntities = jpaGraduatedMemberRepository.findAllByName(name)

        return graduatedMemberEntities.map { fetchWithParts(it) }
    }

    override fun findAllByNicknameKorean(nicknameKorean: String): List<GraduatedMember> {
        val graduatedMemberEntities = jpaGraduatedMemberRepository.findAllByNicknameKoreanIgnoreCase(nicknameKorean)

        return graduatedMemberEntities.map { fetchWithParts(it) }
    }

    override fun findAllByNicknameEnglish(nicknameEnglish: String): List<GraduatedMember> {
        val graduatedMemberEntities = jpaGraduatedMemberRepository.findAllByNicknameEnglishIgnoreCase(nicknameEnglish)

        return graduatedMemberEntities.map { fetchWithParts(it) }
    }
}
