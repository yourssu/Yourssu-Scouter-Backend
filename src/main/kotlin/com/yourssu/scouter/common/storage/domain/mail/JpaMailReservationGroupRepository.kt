package com.yourssu.scouter.common.storage.domain.mail

import org.springframework.data.jpa.repository.JpaRepository

interface JpaMailReservationGroupRepository : JpaRepository<MailReservationGroupEntity, Long> {

    fun findAllBySenderEmail(senderEmail: String): List<MailReservationGroupEntity>

    fun findAllBySenderEmailIn(senderEmails: List<String>): List<MailReservationGroupEntity>
}
