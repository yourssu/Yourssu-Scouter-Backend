package com.yourssu.scouter.common.storage.domain.mail

import org.springframework.data.jpa.repository.JpaRepository

interface JpaMailReservationRepository : JpaRepository<MailReservationEntity, Long> {
}
