package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.hrms.implement.domain.member.WithdrawnMember
import com.yourssu.scouter.hrms.implement.domain.member.WithdrawnMemberRepository
import org.springframework.stereotype.Repository

@Repository
class WithdrawnMemberRepositoryImpl(
    private val jpaWithdrawnMemberRepository: JpaWithdrawnMemberRepository,
    private val jpaMemberPartRepository: JpaMemberPartRepository,
) : WithdrawnMemberRepository {

    override fun findAll(): List<WithdrawnMember> {
        val withdrawnMemberEntities = jpaWithdrawnMemberRepository.findAll()

        return fetchWithParts(withdrawnMemberEntities)
    }

    private fun fetchWithParts(withdrawnMemberEntities: List<WithdrawnMemberEntity>): List<WithdrawnMember> {
        return withdrawnMemberEntities.map { withdrawnMemberEntity ->
            val partEntities = jpaMemberPartRepository.findAllPartsByMemberId(withdrawnMemberEntity.member.id!!)
            val parts: List<Part> = partEntities.map { it.toDomain() }

            withdrawnMemberEntity.toDomain(parts)
        }
    }

    override fun findAllByName(name: String): List<WithdrawnMember> {
        val withdrawnMemberEntities = jpaWithdrawnMemberRepository.findAllByName(name)

        return fetchWithParts(withdrawnMemberEntities)
    }

    override fun findAllByNicknameKorean(nicknameKorean: String): List<WithdrawnMember> {
        val withdrawnMemberEntities = jpaWithdrawnMemberRepository.findAllByNicknameKoreanIgnoreCase(nicknameKorean)

        return fetchWithParts(withdrawnMemberEntities)
    }

    override fun findAllByNicknameEnglish(nicknameEnglish: String): List<WithdrawnMember> {
        val withdrawnMemberEntities = jpaWithdrawnMemberRepository.findAllByNicknameEnglishIgnoreCase(nicknameEnglish)

        return fetchWithParts(withdrawnMemberEntities)
    }
}
