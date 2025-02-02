package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.WithdrawnMember
import com.yourssu.scouter.hrms.implement.domain.member.WithdrawnMemberRepository
import org.springframework.stereotype.Repository

@Repository
class WithdrawnMemberRepositoryImpl(
    private val jpaWithdrawnMemberRepository: JpaWithdrawnMemberRepository,
    private val jpaMemberPartRepository: JpaMemberPartRepository,
) : WithdrawnMemberRepository {

    override fun save(withdrawnMember: WithdrawnMember): WithdrawnMember {
        val savedWithdrawnMemberEntity: WithdrawnMemberEntity =
            jpaWithdrawnMemberRepository.save(WithdrawnMemberEntity.from(withdrawnMember))

        return savedWithdrawnMemberEntity.toDomain(withdrawnMember.member)
    }

    override fun findAll(): List<WithdrawnMember> {
        val withdrawnMemberEntities = jpaWithdrawnMemberRepository.findAll()

        return withdrawnMemberEntities.map { fetchWithParts(it) }
    }

    private fun fetchWithParts(withdrawnMemberEntity: WithdrawnMemberEntity): WithdrawnMember {
        val partEntities = jpaMemberPartRepository.findAllPartsByMemberId(withdrawnMemberEntity.member.id!!)
        val parts: List<Part> = partEntities.map { it.toDomain() }
        val savedMember: Member = withdrawnMemberEntity.member.toDomain(parts)

        return withdrawnMemberEntity.toDomain(savedMember)
    }

    override fun findAllByName(name: String): List<WithdrawnMember> {
        val withdrawnMemberEntities = jpaWithdrawnMemberRepository.findAllByName(name)

        return withdrawnMemberEntities.map { fetchWithParts(it) }
    }

    override fun findAllByNicknameKorean(nicknameKorean: String): List<WithdrawnMember> {
        val withdrawnMemberEntities = jpaWithdrawnMemberRepository.findAllByNicknameKoreanIgnoreCase(nicknameKorean)

        return withdrawnMemberEntities.map { fetchWithParts(it) }
    }

    override fun findAllByNicknameEnglish(nicknameEnglish: String): List<WithdrawnMember> {
        val withdrawnMemberEntities = jpaWithdrawnMemberRepository.findAllByNicknameEnglishIgnoreCase(nicknameEnglish)

        return withdrawnMemberEntities.map { fetchWithParts(it) }
    }
}
