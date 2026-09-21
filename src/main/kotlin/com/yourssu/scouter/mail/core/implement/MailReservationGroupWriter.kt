package com.yourssu.scouter.mail.core.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class MailReservationGroupWriter(
    private val mailReservationGroupRepository: MailReservationGroupRepository,
    private val mailReservationRepository: MailReservationRepository,
) {

    fun save(group: MailReservationGroup): MailReservationGroup =
        mailReservationGroupRepository.save(group)

    fun delete(id: Long) =
        mailReservationGroupRepository.deleteById(id)

    /**
     * 소속 메일들의 현재 상태로부터 그룹 상태를 재계산해 저장한다.
     * 증감이 아닌 재계산이므로 다중 인스턴스·재시도 상황에서도 다음 호출 시 수렴한다.
     * 소속 메일이 없으면 상태를 변경하지 않는다.
     */
    fun syncStatus(groupId: Long) {
        val group = mailReservationGroupRepository.findById(groupId) ?: return
        val statuses = mailReservationRepository.findAllByGroupId(groupId).map { it.status }
        val resolved = MailReservationGroup.resolveStatus(statuses) ?: return
        if (resolved != group.status) {
            mailReservationGroupRepository.updateStatus(groupId, resolved)
        }
    }

    /** SENDING 고착 복구 등 벌크 갱신 이후, 해당 상태로 남은 그룹들을 일괄 재계산한다. */
    fun syncStatusOfGroupsIn(status: MailReservationStatus) {
        mailReservationGroupRepository.findAllByStatus(status).forEach { syncStatus(it.id!!) }
    }
}
