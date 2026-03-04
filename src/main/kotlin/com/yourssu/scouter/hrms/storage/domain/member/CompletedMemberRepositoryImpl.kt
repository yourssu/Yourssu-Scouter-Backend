package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.CompletedMember
import com.yourssu.scouter.hrms.implement.domain.member.CompletedMemberRepository
import com.yourssu.scouter.hrms.implement.domain.member.Member
import org.springframework.stereotype.Repository

@Repository
class CompletedMemberRepositoryImpl(
    private val jpaCompletedMemberRepository: JpaCompletedMemberRepository,
    private val jpaMemberPartRepository: JpaMemberPartRepository,
) : CompletedMemberRepository {

    override fun save(completedMember: CompletedMember): CompletedMember {
        val savedCompletedMemberEntity: CompletedMemberEntity =
            jpaCompletedMemberRepository.save(CompletedMemberEntity.from(completedMember))

        return savedCompletedMemberEntity.toDomain(completedMember.member)
    }

    override fun findByMemberId(memberId: Long): CompletedMember? {
        val completedMemberEntity = jpaCompletedMemberRepository.findByMemberId(memberId)

        return completedMemberEntity?.let { fetchWithParts(it) }
    }

    override fun findAll(): List<CompletedMember> {
        val completedMemberEntities = jpaCompletedMemberRepository.findAll()

        return completedMemberEntities.map { fetchWithParts(it) }
    }

    private fun fetchWithParts(completedMemberEntity: CompletedMemberEntity): CompletedMember {
        val partEntities = jpaMemberPartRepository.findAllPartsByMemberId(completedMemberEntity.member.id!!)
        val parts: List<Part> = partEntities.map { it.toDomain() }
        val savedMember: Member = completedMemberEntity.member.toDomain(parts)

        return completedMemberEntity.toDomain(savedMember)
    }

    override fun findAllByName(name: String): List<CompletedMember> {
        val completedMemberEntities = jpaCompletedMemberRepository.findAllByName(name)

        return completedMemberEntities.map { fetchWithParts(it) }
    }

    override fun findAllByNicknameKorean(nicknameKorean: String): List<CompletedMember> {
        val completedMemberEntities = jpaCompletedMemberRepository.findAllByNicknameKorean(nicknameKorean)

        return completedMemberEntities.map { fetchWithParts(it) }
    }

    override fun findAllByNicknameEnglish(nicknameEnglish: String): List<CompletedMember> {
        val completedMemberEntities = jpaCompletedMemberRepository.findAllByNicknameEnglishIgnoreCase(nicknameEnglish)

        return completedMemberEntities.map { fetchWithParts(it) }
    }

    override fun deleteByMemberId(memberId: Long) {
        jpaCompletedMemberRepository.deleteByMemberId(memberId)
    }
}
