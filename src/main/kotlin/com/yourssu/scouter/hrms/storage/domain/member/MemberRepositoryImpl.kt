package com.yourssu.scouter.hrms.storage.domain.member

import com.yourssu.scouter.common.storage.domain.part.PartEntity
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberRepository
import org.springframework.stereotype.Repository

@Repository
class MemberRepositoryImpl(
    private val jpaMemberRepository: JpaMemberRepository,
    private val jpaMemberPartRepository: JpaMemberPartRepository,
) : MemberRepository {

    override fun save(member: Member): Member {
        val savedMemberEntity: MemberEntity =
            jpaMemberRepository.save(MemberEntity.from(member))
        val memberPartEntities = member.parts.map {
            MemberPartEntity(
                member = savedMemberEntity,
                part = PartEntity.from(it)
            )
        }
        jpaMemberPartRepository.saveAll(memberPartEntities)

        return savedMemberEntity.toDomain(member.parts)
    }
}
