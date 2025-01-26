package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class MemberRepositoryImpl(
    private val jpaMemberRepository: JpaMemberRepository,
) : MemberRepository {

    override fun save(member: Member) = jpaMemberRepository.save(MemberEntity.from(member)).toDomain()

    override fun findById(memberId: Long): Member? = jpaMemberRepository.findByIdOrNull(memberId)?.toDomain()

    override fun findAll(): List<Member> = jpaMemberRepository.findAll().map { it.toDomain() }

    override fun findAllByName(name: String): List<Member> =
        jpaMemberRepository.findAllByName(name).map { it.toDomain() }

    override fun findAllByNicknameKorean(nicknameKorean: String): List<Member> =
        jpaMemberRepository.findAllByNicknameKorean(nicknameKorean).map { it.toDomain() }

    override fun findAllByNicknameEnglish(nicknameEnglish: String): List<Member> =
        jpaMemberRepository.findAllByNicknameEnglish(nicknameEnglish).map { it.toDomain() }

    override fun deleteById(memberId: Long) {
        jpaMemberRepository.deleteById(memberId)
    }
}
