package com.yourssu.scouter.member.core.business

import com.yourssu.scouter.masterdata.part.implement.Part
import com.yourssu.scouter.masterdata.part.implement.PartReader
import com.yourssu.scouter.member.core.business.dto.AdminMemberPartDto
import com.yourssu.scouter.member.core.implement.Member
import com.yourssu.scouter.member.core.implement.MemberPartReader
import com.yourssu.scouter.member.core.implement.MemberPartWriter
import com.yourssu.scouter.member.core.implement.MemberReader
import com.yourssu.scouter.member.core.implement.MemberState
import com.yourssu.scouter.member.support.exception.IllegalMemberPartAssignmentException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 어드민 파트 배정 화면 전용. 기존 MemberService 와 REST API 의 동작은 그대로 두고 여기에만 새 경로를 둔다.
 *
 * 목록은 MemberService.readAllActiveByFilters 를 쓰지 않는다. 그 메서드의 members.sorted() 가
 * Member.compareTo -> parts.first() 를 타서, 파트가 0개인 멤버가 섞여 있으면 터진다.
 * 그런데 이 화면이 고쳐야 할 대상이 바로 그 파트 0개 멤버다.
 */
@Service
class AdminMemberPartService(
    private val memberReader: MemberReader,
    private val memberPartReader: MemberPartReader,
    private val memberPartWriter: MemberPartWriter,
    private val partReader: PartReader,
) {

    fun readAllByFilters(state: MemberState, search: String?, partId: Long?): List<AdminMemberPartDto> {
        val members: List<Member> = readMembers(state, search)
        val filtered = if (partId == null) {
            members
        } else {
            members.filter { member -> member.parts.any { it.id == partId } }
        }

        // 파트가 없는 멤버를 맨 앞에 둔다. 이 화면에서 제일 먼저 손봐야 할 대상이다.
        return filtered
            .sortedWith(
                compareBy(
                    { it.parts.isNotEmpty() },
                    { it.parts.firstOrNull()?.name ?: "" },
                    { it.name },
                ),
            )
            .map(AdminMemberPartDto::from)
    }

    private fun readMembers(state: MemberState, search: String?): List<Member> {
        val keyword: String? = search?.takeIf(String::isNotBlank)

        return when (state) {
            MemberState.ACTIVE ->
                if (keyword == null) memberReader.readAllActive().map { it.member }
                else memberReader.searchAllActiveByNameOrNickname(keyword).map { it.member }

            MemberState.INACTIVE ->
                if (keyword == null) memberReader.readAllInactive().map { it.member }
                else memberReader.searchAllInactiveByNameOrNickname(keyword).map { it.member }

            MemberState.COMPLETED ->
                if (keyword == null) memberReader.readAllCompleted().map { it.member }
                else memberReader.searchAllCompletedByNameOrNickname(keyword).map { it.member }

            MemberState.GRADUATED ->
                if (keyword == null) memberReader.readAllGraduated().map { it.member }
                else memberReader.searchAllGraduatedByNameOrNickname(keyword).map { it.member }

            MemberState.WITHDRAWN ->
                if (keyword == null) memberReader.readAllWithdrawn().map { it.member }
                else memberReader.searchAllWithdrawnByNameOrNickname(keyword).map { it.member }
        }
    }

    @Transactional
    fun assignPart(memberId: Long, partId: Long) {
        memberReader.readById(memberId)
        val part: Part = partReader.readById(partId)

        if (memberPartReader.hasPart(memberId, partId)) {
            throw IllegalMemberPartAssignmentException("이미 ${part.name} 파트가 배정되어 있습니다.")
        }

        memberPartWriter.assign(memberId, partId)
    }

    /**
     * 마지막 파트는 해제하지 않는다. Member.compareTo 와 Member.makeRoleUpdatedMessage 가 parts.first() 를 쓰기 때문에,
     * 파트 0개 멤버를 새로 만들면 이 화면이 멤버 목록 조회 500 을 직접 유발하게 된다.
     * 파트를 옮기려면 새 파트를 먼저 배정하고 옛 파트를 해제하면 된다.
     */
    @Transactional
    fun unassignPart(memberId: Long, partId: Long) {
        memberReader.readById(memberId)
        val part: Part = partReader.readById(partId)

        if (!memberPartReader.hasPart(memberId, partId)) {
            throw IllegalMemberPartAssignmentException("${part.name} 파트가 배정되어 있지 않습니다.")
        }
        if (memberPartReader.countParts(memberId) <= 1) {
            throw IllegalMemberPartAssignmentException(
                "마지막 파트는 해제할 수 없습니다. 파트를 옮기려면 새 파트를 먼저 배정하세요.",
            )
        }

        memberPartWriter.unassign(memberId, partId)
    }
}
