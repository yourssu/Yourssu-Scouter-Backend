package com.yourssu.scouter.member.core.storage

import com.yourssu.scouter.masterdata.part.storage.JpaPartRepository
import com.yourssu.scouter.member.core.implement.MemberPartRepository
import org.springframework.stereotype.Repository

@Repository
class MemberPartRepositoryImpl(
    private val jpaMemberPartRepository: JpaMemberPartRepository,
    private val jpaMemberRepository: JpaMemberRepository,
    private val jpaPartRepository: JpaPartRepository,
) : MemberPartRepository {

    override fun existsAssignment(memberId: Long, partId: Long): Boolean {
        return jpaMemberPartRepository.existsByMemberIdAndPartId(memberId, partId)
    }

    override fun countDistinctParts(memberId: Long): Long {
        return jpaMemberPartRepository.countDistinctPartsByMemberId(memberId)
    }

    override fun assign(memberId: Long, partId: Long) {
        jpaMemberPartRepository.save(
            MemberPartEntity(
                member = jpaMemberRepository.getReferenceById(memberId),
                part = jpaPartRepository.getReferenceById(partId),
            ),
        )
    }

    override fun unassign(memberId: Long, partId: Long) {
        // 유니크 제약이 없어 같은 (member, part) 가 여러 행으로 들어가 있을 수 있다. 남김없이 지운다.
        jpaMemberPartRepository.deleteAll(jpaMemberPartRepository.findAllByMemberIdAndPartId(memberId, partId))
    }
}
